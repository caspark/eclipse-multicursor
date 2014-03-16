package com.asparck.eclipse.multicursor.util;

import static org.junit.Assert.*;

import org.eclipse.swt.graphics.Point;
import org.junit.Test;

public class TextUtilTest {

	@Test
	public void findWordSurroundingShouldFindWordSurroundingGivenOffset() {
		String str = "my name is bob";
		Point word = TextUtil.findWordSurrounding(str, 5);

		assertEquals("name", str.substring(word.x, word.x + word.y));
	}

	@Test
	public void findWordSurroundingShouldFindWordWhenOffsetIsAtStartOfWord() {
		String str = "my name is bob";
		Point word = TextUtil.findWordSurrounding(str, 3);

		assertEquals("name", str.substring(word.x, word.x + word.y));
	}
	
	@Test
	public void findWordSurroundingShouldFindWordWhenOffsetIsAtEndOfWord() {
		String str = "my name is bob";
		Point word = TextUtil.findWordSurrounding(str, 7);

		assertEquals("name", str.substring(word.x, word.x + word.y));
	}
	
	@Test
	public void findWordSurroundingShouldFindWordWhenOffsetIsAtStartOfString() {
		String str = "my name is bob";
		Point word = TextUtil.findWordSurrounding(str, 0);

		assertEquals("my", str.substring(word.x, word.x + word.y));
	}
	
	@Test
	public void findWordSurroundingShouldFindWordWhenOffsetIsAtEndOfString() {
		String str = "my name is bob";
		Point word = TextUtil.findWordSurrounding(str, str.length());

		assertEquals("bob", str.substring(word.x, word.x + word.y));
	}
	
	@Test
	public void findWordSurroundingShouldReturnNullIfOffsetIsInEmptyString() {
		Point word = TextUtil.findWordSurrounding("", 0);
		
		assertNull(word);
	}

	@Test
	public void findWordSurroundingShouldReturnNullIfOffsetIsInWhitespace() {
		Point word = TextUtil.findWordSurrounding("hello  world", 6);
		
		assertNull(word);
	}
}
