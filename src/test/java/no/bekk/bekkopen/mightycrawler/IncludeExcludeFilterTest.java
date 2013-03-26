package no.bekk.bekkopen.mightycrawler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.bekk.bekkopen.mightycrawler.IncludeExcludeFilter;

import org.junit.Test;


public class IncludeExcludeFilterTest {

	private IncludeExcludeFilter filter;
	
	@Test
	public void testSingleInclude() {
		filter = new IncludeExcludeFilter("text", "");
		assertTrue(filter.letsThrough("text"));
	}
	
	@Test
	public void testIncludeAndExclude() {
		filter = new IncludeExcludeFilter("te.*", "text");
		assertFalse(filter.letsThrough("text"));
		assertTrue(filter.letsThrough("test"));
	}

	@Test
	public void testMultipleIncludes() {
		filter = new IncludeExcludeFilter("text|fine", "");
		assertTrue(filter.letsThrough("fine"));
		assertTrue(filter.letsThrough("text"));
	}

	@Test
	public void testMultipleIncludesAndExcludes() {
		filter = new IncludeExcludeFilter("te.*|se.*", "tex.*|send");

		assertTrue(filter.letsThrough("semi"));
		assertTrue(filter.letsThrough("tell"));
		assertTrue(filter.letsThrough("te"));
		assertFalse(filter.letsThrough("tex"));
		assertFalse(filter.letsThrough("texter"));
		assertFalse(filter.letsThrough("send"));
	}
}
