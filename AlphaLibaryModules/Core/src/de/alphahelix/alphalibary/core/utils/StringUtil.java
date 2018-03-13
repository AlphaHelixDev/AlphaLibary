package de.alphahelix.alphalibary.core.utils;

import com.google.common.base.Strings;
import org.bukkit.ChatColor;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class StringUtil {
	
	public static String generateRandomString(int size) {
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < size; i++) {
			char c = chars[ThreadLocalRandom.current().nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String getProgessBar(int current, int maximum, int total, char symbol, ChatColor completed, ChatColor uncompleted) {
		float percent = current / maximum;
		int progress = (int) (total * percent);
		
		return Strings.repeat("" + completed + symbol, progress)
				+ Strings.repeat("" + uncompleted + symbol, total - progress);
	}
	
	public static boolean isLong(String s) {
		Scanner sc = new Scanner(s.trim());
		
		if(!sc.hasNextLong()) return false;
		
		sc.nextLong();
		return !sc.hasNext();
	}
	
	public static boolean isDouble(String s) {
		Scanner sc = new Scanner(s.trim());
		
		if(!sc.hasNextDouble()) return false;
		
		sc.nextDouble();
		return !sc.hasNext();
	}
	
	public static String getFirstColors(String input) {
		StringBuilder result = new StringBuilder();
		int length = input.length();
		
		for(int index = 0; index < length; index++) {
			char section = input.charAt(index);
			
			if(section == ChatColor.COLOR_CHAR && index < length - 1) {
				char c = input.charAt(index + 1);
				ChatColor color = ChatColor.getByChar(c);
				
				if(color != null) {
					result.insert(0, color.toString());
					
					if(color.isColor() || color.equals(ChatColor.RESET)) {
						break;
					}
				}
			}
		}
		return result.toString();
	}
	
	public static String repeat(String string, int count) {
		if(count <= 1) {
			return count == 0 ? "" : string;
		} else {
			int len = string.length();
			long longSize = (long) len * (long) count;
			int size = (int) longSize;
			if((long) size != longSize) {
				throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
			} else {
				char[] array = new char[size];
				string.getChars(0, len, array, 0);
				int n;
				for(n = len; n < size - n; n <<= 1) {
					System.arraycopy(array, 0, array, n, n);
				}
				System.arraycopy(array, 0, array, n, size - n);
				return new String(array);
			}
		}
	}
	
	public static String replaceLast(String string, String toReplace, String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if(pos > -1) {
			return string.substring(0, pos)
					+ replacement
					+ string.substring(pos + toReplace.length(), string.length());
		} else {
			return string;
		}
	}
	
	public static List<String> upperEverything(List<String> list) {
		List<String> nL = new LinkedList<>();
		for(String str : list)
			nL.add(str.toUpperCase());
		return nL;
	}
	
	public static List<String> lowerEverything(List<String> list) {
		List<String> nL = new LinkedList<>();
		for(String str : list)
			nL.add(str.toLowerCase());
		return nL;
	}
	
}
