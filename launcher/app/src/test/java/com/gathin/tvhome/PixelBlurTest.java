package com.gathin.tvhome;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;
public class PixelBlurTest {
    @Test public void uniformWallpaperAndZeroBlurStayUnchanged() {
        int[] uniform = new int[15]; Arrays.fill(uniform, 0xFF3278B2);
        int[] expected = uniform.clone(); PixelBlur.apply(uniform, 5, 3, 20); assertArrayEquals(expected, uniform);
        int[] sharp = {0xFF000000, 0xFFFFFFFF}; expected = sharp.clone(); PixelBlur.apply(sharp, 2, 1, 0); assertArrayEquals(expected, sharp);
    }
    @Test public void blendsAcrossRowsAndColumnsAndHandlesSinglePixel() {
        int[] edge = {0xFF000000,0xFF000000,0xFFFFFFFF,0xFFFFFFFF};
        PixelBlur.apply(edge,2,2,1); assertTrue((edge[0]&255)>0); assertTrue((edge[3]&255)<255);
        int[] one = {0xFFAB34CD}; PixelBlur.apply(one,1,1,24); assertEquals(0xFFAB34CD,one[0]);
    }
    @Test(expected=IllegalArgumentException.class) public void rejectsBrokenDimensions() { PixelBlur.apply(new int[2],3,2,4); }
}
