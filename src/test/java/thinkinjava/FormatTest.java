package thinkinjava;

import org.junit.Test;

import java.util.Date;

/**
 * 字符格式化测试类
 * 
 * @author Administrator
 *
 */
public class FormatTest {

	@Test
	public void test() {
		String format = String.format("[%1$TY年%1$Tm月%1$Td日 %1$TH:%1$TM:%1$TS]",new Date());
		System.out.println(format);
		System.out.println(new Date());
	}

}
