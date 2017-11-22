package boc.gcs.batch.report.render;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * 字符串表达式计算类
 * 表达式的组成:
 * 1、数字，包括正负整数小数
 * 2、运算符：+ - / * %
 * 3、圆括号
 * 4、变量
 * @author fy
 */
public class ExpressionParser {
	// 4种标记类型
	private static final int NONE_TOKEN = 0; // 标记为空或者结束符
	private static final int DELIMITER_TOKEN = 1; // 标记为分隔符
	private static final int VARIABLE_TOKEN = 2; // 标记为变量
	private static final int NUMBER_TOKEN = 3; // 标记为数字
	private static final String EOE = "\0";  // 表达式的结束标记
	
	// 5种错误类型
	private static final String SYNTAX_ERROR = "语法错误"; 
	private static final String UNBALPARENS_ERROR = "括号没有结束错误"; 
	private static final String NOEXP_ERROR = "表达式为空错误"; 
	private static final String DIVBYZERO_ERROR = "被0除错误";  
	private static final String VARIABLE_NOTEXIST= "变量的值不存在"; 


	private String exp=""; // 表达式字符串
	private int expIndex=0; // 解析器当前指针在表达式中的位置
	private String token=""; // 解析器当前处理的标记
	private int tokenType=0; // 解析器当前处理的标记类型
	private Map<String,BigDecimal> varsMap = new HashMap<String, BigDecimal>(); // 变量的值
	private List<String> variableNames = new ArrayList<String>(); //表达式中变量的名称
	private boolean isSimpleExp = true;  //是否简单表达式，指仅包含变量和+-*/元素且+-*/只出现一种的表达式
	private char simpleOperator = ' ';  // +-/*  中的一种
	
	private static Logger log = Logger.getLogger(ExpressionParser.class);
	/**
	 * 构造器
	 * @param expStr 字符串表达式
	 * @throws RuntimeException 
	 */
	public ExpressionParser(String expStr){
		if(expStr==null || expStr.trim().equals("")){
			this.handleError(NOEXP_ERROR); // 没有表达式异常
		}
		
		this.exp = expStr.trim();
		
		analyseExp();
	}

	 
	/**
	 * 分析表达式是否为简单表达式,提取表达式中的变量
	 * @throws RuntimeException 
	 */
	private void analyseExp(){
		clear();
		String operator = "+-*/";
		
		this.getToken();
		if (EOE.equals(this.token)) {
			this.handleError(NOEXP_ERROR); // 没有表达式异常
		}

		while(!EOE.equals(token)){
			if(tokenType==VARIABLE_TOKEN){
				this.variableNames.add(token);
			}else if(tokenType==DELIMITER_TOKEN && operator.indexOf(token)!= -1){ //分隔符为+-*/
				if(this.simpleOperator==' ')	{
					this.simpleOperator = token.charAt(0);
				}
				if(simpleOperator!=token.charAt(0)){ //分隔符不同，则不是简单表达式
					this.isSimpleExp = false;
				}
			}else{ //如果不只包含变量和+-*/,则不是简单表达式
				this.isSimpleExp = false;
			}
			
			this.getToken();
		}
		
		//没有变量，则不是简单表达式
		if(variableNames.size()<1){
			this.isSimpleExp = false;
		}
		
	}
	
	
	
	/**
	 * 得到表达式中的变量名称
	 * @return 变量名称,第一个变量为list.get(0)
	 */
	public List<String> getVariableNames(){
		return variableNames;
	}
	
	
	
	/**
	 * 返回表达式的值,表达式中无变量
	 * @return 表达式计算后的值
	 * @throws RuntimeException
	 */
	public BigDecimal evaluate() {
		return evaluate(null);
	}
	
	
	
	/**
	 * 返回表达式的值, 表达式中有变量
	 * @param vars 表达式中 变量的值，Map中key为变量的名称，value为变量的值
	 * @return  表达式计算后的值
	 * @throws RuntimeException
	 */
	public BigDecimal evaluate(Map<String,BigDecimal> varsMap){
		double result;
		clear();
		this.varsMap = varsMap;
		
		//如果是简单表达式
		if(isSimpleExp){
			return new BigDecimal(parseSimpleExp());
		}
		
		
		// 获取第一个标记
		this.getToken();

		result = this.parseAddOrSub(); // 处理加减语句
		// 处理完加减语句，应该就是表达式结束符，如果不是，则返回异常
		if (!this.token.equals(EOE)) {
			this.handleError(SYNTAX_ERROR);
		}
		return new BigDecimal(result);
	}

	
	
	/**
	 * 计算简单表达式
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double parseSimpleExp(){
		double result=0;
		if(this.varsMap.get(this.variableNames.get(0))!=null){
			result = this.varsMap.get(this.variableNames.get(0)).doubleValue();
		}else{
			this.handleError(this.variableNames.get(0)+VARIABLE_NOTEXIST);
		}
		
		for(int i = 1 ; i<variableNames.size(); i++){
			if(this.varsMap.get(this.variableNames.get(i))==null){
				this.handleError(this.variableNames.get(i)+VARIABLE_NOTEXIST);
				return 0;
			}
			double temp = this.varsMap.get(this.variableNames.get(i)).doubleValue();
			
			switch (simpleOperator){
			case '+':
				result+=temp ;
				break;
			case '-':
				result-=temp ;
				break;
			case '*':
				result*=temp ;
				break;
			case '/':
				result/=temp ;
				break;
			default:
				this.handleError(SYNTAX_ERROR);	
				break;
			}
		}
		
		return result;
	}
	
	

	/**
	 * 计算加减法表达式
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double parseAddOrSub(){
		char op; // 运算符
		double result; // 结果
		double partialResult; // 子表达式的结果

		result = this.pareseMulOrDiv(); // 先用乘除法计算当前表达式的值
		// 如果当前标记的第一个字母是加减号，则继续进行加减运算
		while ((op = this.token.charAt(0)) == '+' || op == '-') {
			this.getToken(); // 取下一个标记
			// 用乘除法计算当前子表达式的值
			partialResult = this.pareseMulOrDiv();
			switch (op) {
			case '-':
				// 如果是减法，则用已处理的子表达式的值减去当前子表达式的值
				result = result - partialResult;
				break;
			case '+':
				// 如果是加法，用已处理的子表达式的值加上当前子表达式的值
				result = result + partialResult;
				break;
			}
		}
		return result;
	}

	
	
	/**
	 * 计算乘除法表达式，包括取模运算
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double pareseMulOrDiv(){
		char op; // 运算符
		double result; // 结果
		double partialResult; // 子表达式结果
		// 先用一元运算计算当前子表达式的值
		result = this.parseUnaryOperator();
		// 如果当前标记的第一个字母是乘、除或者取模运算，则继续进行乘除法运算
		while ((op = this.token.charAt(0)) == '*' || op == '/' || op == '%') {
			this.getToken(); // 取下一标记
			// 用一元运算计算当前子表达式的值
			partialResult = this.parseUnaryOperator();
			switch (op) {
			case '*':
				// 如果是乘法，则用已处理子表达式的值乘以当前子表达式的值
				result = result * partialResult;
				break;
			case '/':
				// 如果是除法，判断当前字表达式的值是否为0，如果为0，则抛出被0除异常
				if (partialResult == 0.0) {
					this.handleError(DIVBYZERO_ERROR);
				}
				// 除数不为0，则进行除法运算
				result = result / partialResult;
				break;
			case '%':
				// 如果是取模运算，也要判断当前子表达式的值是否为0
				if (partialResult == 0.0) {
					this.handleError(DIVBYZERO_ERROR);
				}
				result = result % partialResult;
				break;
			}
		}
		return result;
	}

	

	/**
	 * 计算一元运算，＋，－，表示正数和负数
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double parseUnaryOperator(){
		double result; // 结果
		String op; // 运算符
		op = "";
		// 如果当前标记类型为分隔符，而且分隔符的值等于+或者-
		if ((this.tokenType == DELIMITER_TOKEN) && this.token.equals("+")
				|| this.token.equals("-")) {
			op = this.token;
			this.getToken();
		}
		// 用括号运算计算当前子表达式的值
		result = this.parseBracket();
		if (op.equals("-")) {
			// 如果运算符为-，则表示负数，将子表达式的值变为负数
			result = -result;
		}
		return result;
	}

	
	
	/**
	 * 计算括号运算
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double parseBracket(){
		double result; // 结果
		// 如果当前标记为左括号，则表示是一个括号运算
		if (this.token.equals("(")) {
			this.getToken(); // 取下一标记
			result = this.parseAddOrSub(); // 用加减法运算计算子表达式的值
			// 如果当前标记不等于右括号，抛出括号不匹配异常
			if (!this.token.equals(")")) {
				this.handleError(UNBALPARENS_ERROR);
			}
			this.getToken(); // 否则取下一个标记
		} else {
			// 如果不是左括号，表示不是一个括号运算，则用原子元素运算计算子表达式值
			result = this.parseAtomElement();
		}
		return result;
	}

	
	/**
	 * 计算原子元素运算，包括变量和数字
	 * @return 计算后的值
	 * @throws RuntimeException
	 */
	private double parseAtomElement(){
		double result = 0.0; // 结果

		switch (this.tokenType) {
		case NUMBER_TOKEN:
			// 如果当前标记类型为数字
			try {
				// 将数字的字符串转换成数字值
				result = Double.parseDouble(this.token);
			} catch (NumberFormatException exc) {
				this.handleError(SYNTAX_ERROR);
			}
			this.getToken(); // 取下一个标记
			break;
		case VARIABLE_TOKEN:
			// 如果当前标记类型是变量，则取变量的值
			result = this.findVar(token);
			this.getToken();
			break;
		default:
			this.handleError(SYNTAX_ERROR);
			break;
		}
		return result;
	}

	
	
	/**
	 * 根据变量名获取变量的值
	 * @param vname
	 * @return 变量的值
	 * @throws RuntimeException
	 */
	private double findVar(String vname){
		if (!Character.isLetter(vname.charAt(0))) {
			this.handleError(SYNTAX_ERROR);
			return 0.0;
		}
		
		//如果变量的值不存在
		if(varsMap.get(vname) == null){
			this.handleError(vname+VARIABLE_NOTEXIST);
			return 0.0;
		}
		
		// 从实例变量数组vars中取出该变量的值
		return varsMap.get(vname).doubleValue();
	}

	


	/**
	 * 处理异常情况
	 * @param error 异常说明文字
	 * @throws RuntimeException
	 */
	private void handleError(String error){
		log.error(error);
		
		// 遇到异常情况时，将异常提示信息封装在异常中抛出
		throw new RuntimeException(error);
	}

	
	/**
	 * 获取下一个标记
	 */
	private void getToken() {
		// 设置初始值
		this.token = "";
		this.tokenType = NONE_TOKEN;

		// 检查表达式是否结束，如果解析器当前指针已经到达了字符串长度，
		// 则表明表达式已经结束，置当前标记的值为EOE
		if (this.expIndex == this.exp.length()) {
			this.token = EOE;
			return;
		}

		// 跳过表达式中的空白符
		while (this.expIndex < this.exp.length()
				&& Character.isWhitespace(this.exp.charAt(this.expIndex))) {
			++this.expIndex;
		}

		// 再次检查表达式是否结束
		if (this.expIndex == this.exp.length()) {
			this.token = EOE;
			return;
		}

		// 取得解析器当前指针指向的字符
		char currentChar = this.exp.charAt(this.expIndex);
		// 如果当前字符是一个分隔符，则认为这是一个分隔符标记
		// 给当前标记和标记类型赋值，并将指针后移
		if (isDelim(currentChar)) {
			this.token += currentChar;
			this.expIndex++;
			this.tokenType = DELIMITER_TOKEN;
		} else if (Character.isLetter(currentChar)) {
			// 如果当前字符是一个字母，则认为是一个变量标记
			// 将解析器指针往后移，知道遇到一个分隔符，之间的字符都是变量的组成部分
			while (!isDelim(currentChar)) {
				this.token += currentChar;
				this.expIndex++;
				if (this.expIndex >= this.exp.length()) {
					break;
				} else {
					currentChar = this.exp.charAt(this.expIndex);
				}
			}
			this.tokenType = VARIABLE_TOKEN; // 设置标记类型为变量
		} else if (Character.isDigit(currentChar)) {
			// 如果当前字符是一个数字，则认为当前标记的类型为数字
			// 将解析器指针后移，知道遇到一个分隔符，之间的字符都是该数字的组成部分
			while (!isDelim(currentChar)) {
				this.token += currentChar;
				this.expIndex++;
				if (this.expIndex >= this.exp.length()) {
					break;
				} else {
					currentChar = this.exp.charAt(this.expIndex);
				}
			}
			this.tokenType = NUMBER_TOKEN; // 设置标记类型为数字
		} else {
			// 无法识别的字符，则认为表达式结束
			this.token = EOE;
			return;
		}
	}

	
	
	/**
	 * 判断一个字符是否为分隔符 表达式中的字符包括：
	 * 加“＋”、减“－”、乘“*”、除“/”、取模“%”、左括号“（”、右括号“）”
	 */
	private boolean isDelim(char c) {
		if (("+-*/%()".indexOf(c) != -1))
			return true;
		return false;
	}

	
	
	/**
	 * 清除变量的值
	 */
	private void clear(){
		this.expIndex = 0;
		this.token = "";
		this.tokenType = 0;
		this.varsMap = null;
	}
	
	
}