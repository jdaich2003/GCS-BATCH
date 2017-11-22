package boc.gcs.batch.report.template;

import java.text.Format;

public class ReportTemplateCell {
	private String bindKey = ""; // 绑定数据key
	
	private String text = ""; // 报表头单元格显示的文字
	private boolean isSequence = false; // 是否序号列
	private int colspan = 0; // 单元格跨行
	private String expression = ""; // 表达式，该列为计算列
	private String defaultValue = ""; // 该列固定值
	private Format format = null; // 该列数据格式化对象
	private String convert = ""; // 该数据项由那个类转换
	private int size = 0; // 一行最长多少字符，超过多行显示

	private String type = ""; // 数据类型
	public String getConvert() {
		return convert;
	}

	public void setConvert(String convert) {
		if (convert != null) {
			this.convert = convert.trim();
		}
	}

	public String getBindKey() {
		return bindKey;
	}

	public void setBindKey(String bindKey) {
		if (bindKey != null) {
			this.bindKey = bindKey.trim();
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		if (text != null) {
			this.text = text.trim();
		}
	}

	public boolean isSequence() {
		return isSequence;
	}

	public void setSequence(boolean isSequence) {
		this.isSequence = isSequence;
	}

	public int getColspan() {
		return colspan;
	}

	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		if (expression != null) {
			this.expression = expression.trim();
		}
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		if (defaultValue != null) {
			this.defaultValue = defaultValue.trim();
		}
	}

	public Format getFormat() {
		return format;
	}

	public void setFormat(Format format) {
		this.format = format;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
