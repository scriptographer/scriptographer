/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 * 
 * File created on Aug 17, 2009.
 *
 * $Id$
 */

package com.scriptographer.ui;

import com.scratchdisk.util.IntegerEnum;

/**
 * @author lehni
 *
 */
/* 
 * TODO Merge with Tracker constants and onKeyDown handling?
 */
public enum KeyCode implements IntegerEnum {
	ENTER('\n'),
	BACKSPACE('\b'),
	TAB('\t'),
	CANCEL(3),
	CLEAR(12),
	SHIFT(16),
	CONTROL(17),
	ALT(18),
	PAUSE(19),
	CAPS_LOCK(20),
	ESCAPE(27),
	SPACE(' '),
	PAGE_UP(33),
	PAGE_DOWN(34),
	END(35),
	HOME(36),
	LEFT(37),
	UP(38),
	RIGHT(39),
	DOWN(40),
	COMMA(','),
	MINUS('-'),
	PERIOD('.'),
	SLASH('/'),
	// Shame we can't just used numbers as names
	NUM_0('0'),
	NUM_1('1'),
	NUM_2('2'),
	NUM_3('3'),
	NUM_4('4'),
	NUM_5('5'),
	NUM_6('6'),
	NUM_7('7'),
	NUM_8('8'),
	NUM_9('9'),
	SEMICOLON(';'),
	EQUALS('='),
	A('A'),
	B('B'),
	C('C'),
	D('D'),
	E('E'),
	F('F'),
	G('G'),
	H('H'),
	I('I'),
	J('J'),
	K('K'),
	L('L'),
	M('M'),
	N('N'),
	O('O'),
	P('P'),
	Q('Q'),
	R('R'),
	S('S'),
	T('T'),
	U('U'),
	V('V'),
	W('W'),
	X('X'),
	Y('Y'),
	Z('Z'),
	OPEN_BRACKET('['),
	CLOSE_BRACKET(']'),
	BACKSLASH('\\'),
	NUMPAD_0(96),
	NUMPAD_1(97),
	NUMPAD_2(98),
	NUMPAD_3(99),
	NUMPAD_4(100),
	NUMPAD_5(101),
	NUMPAD_6(102),
	NUMPAD_7(103),
	NUMPAD_8(104),
	NUMPAD_9(105),
	MULTIPLY(106),
	ADD(107),
	SEPARATOR(108),
	SUBTRACT(109),
	DECIMAL(110),
	DIVIDE(111),
	DELETE(127),
	NUM_LOCK(144),
	SCROLL_LOCK(145),
	F1(112),
	F2(113),
	F3(114),
	F4(115),
	F5(116),
	F6(117),
	F7(118),
	F8(119),
	F9(120),
	F10(121),
	F11(122),
	F12(123),
	PRINTSCREEN(154),
	INSERT(155),
	HELP(156),
	META(157),
	BACK_QUOTE(192),
	QUOTE(222),
	KP_UP(224),
	KP_DOWN(225),
	KP_LEFT(226),
	KP_RIGHT(227),
	DEAD_GRAVE(128),
	DEAD_ACUTE(129),
	DEAD_CIRCUMFLEX(130),
	DEAD_TILDE(131),
	DEAD_MACRON(132),
	DEAD_BREVE(133),
	DEAD_ABOVEDOT(134),
	DEAD_DIAERESIS(135),
	DEAD_ABOVERING(136),
	DEAD_DOUBLEACUTE(137),
	DEAD_CARON(138),
	DEAD_CEDILLA(139),
	DEAD_OGONEK(140),
	DEAD_IOTA(141),
	DEAD_VOICED_SOUND(142),
	DEAD_SEMIVOICED_SOUND(143),
	AMPERSAND(150),
	ASTERISK(151),
	QUOTEDBL(152),
	LESS(153),
	GREATER(160),
	BRACELEFT(161),
	BRACERIGHT(162),
	FINAL(24),
	CONVERT(28),
	NONCONVERT(29),
	ACCEPT(30),
	MODECHANGE(31),
	KANA(21),
	KANJI(25),
	ALPHANUMERIC(240),
	KATAKANA(241),
	HIRAGANA(242),
	FULL_WIDTH(243),
	HALF_WIDTH(244),
	ROMAN_CHARACTERS(245);

	protected int code;

	private KeyCode(int code) {
		this.code = code;
	}

	public int value() {
		return code;
	}
}
