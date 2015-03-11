/*
 *  Javelin Stamp Direct Interface
 *
 *  Copyright (c) 1997-2005 Christopher Waters. All rights reserved.
 *  http://www.parallax.com/javelin/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "Java.h"
               
// Java VM assembly language opcodes.
char *gJavaOps[] = {
"nop", 
"aconst_null", 
"iconst_m1", 
"iconst_0", 
"iconst_1", 
"iconst_2", 
"iconst_3", 
"iconst_4", 
"iconst_5", 
"lconst_0", 
"lconst_1", 
"fconst_0", 
"fconst_1", 
"fconst_2", 
"dconst_0", 
"dconst_1", 
"bipush", 
"sipush", 
"ldc", 
"ldc_w", 
"ldc2_w", 
"iload", 
"lload", 
"fload", 
"dload", 
"aload", 
"iload_0", 
"iload_1", 
"iload_2", 
"iload_3", 
"lload_0", 
"lload_1", 
"lload_2", 
"lload_3", 
"fload_0", 
"fload_1", 
"fload_2", 
"fload_3", 
"dload_0", 
"dload_1", 
"dload_2", 
"dload_3", 
"aload_0", 
"aload_1", 
"aload_2", 
"aload_3", 
"iaload", 
"laload", 
"faload", 
"daload", 
"aaload", 
"baload",
"caload", 
"saload", 
"istore", 
"lstore", 
"fstore", 
"dstore", 
"astore", 
"istore_0", 
"istore_1", 
"istore_2", 
"istore_3", 
"lstore_0", 
"lstore_1", 
"lstore_2", 
"lstore_3", 
"fstore_0", 
"fstore_1", 
"fstore_2", 
"fstore_3", 
"dstore_0", 
"dstore_1", 
"dstore_2", 
"dstore_3", 
"astore_0", 
"astore_1", 
"astore_2", 
"astore_3", 
"iastore", 
"lastore", 
"fastore", 
"dastore", 
"aastore", 
"bastore", 
"castore", 
"sastore", 
"pop", 
"pop2", 
"dup", 
"dup_x1", 
"dup_x2", 
"dup2", 
"dup2_x1", 
"dup2_x2", 
"swap", 
"iadd", 
"ladd", 
"fadd", 
"dadd", 
"isub", 
"lsub", 
"fsub", 
"dsub", 
"imul", 
"lmul", 
"fmul", 
"dmul", 
"idiv", 
"ldiv", 
"fdiv", 
"ddiv", 
"irem", 
"lrem", 
"frem", 
"drem", 
"ineg", 
"lneg", 
"fneg", 
"dneg", 
"ishl", 
"lshl", 
"ishr", 
"lshr", 
"iushr", 
"lushr", 
"iand", 
"land", 
"ior", 
"lor", 
"ixor", 
"lxor", 
"iinc", 
"i2l", 
"i2f", 
"i2d", 
"l2i", 
"l2f", 
"l2d", 
"f2i", 
"f2l", 
"f2d", 
"d2i", 
"d2l", 
"d2f", 
"i2b", 
"i2c", 
"i2s", 
"lcmp", 
"fcmpl", 
"fcmpg", 
"dcmpl", 
"dcmpg", 
"ifeq", 
"ifne", 
"iflt", 
"ifge", 
"ifgt", 
"ifle", 
"if_icmpeq", 
"if_icmpne", 
"if_icmplt", 
"if_icmpge", 
"if_icmpgt", 
"if_icmple", 
"if_acmpeq", 
"if_acmpne", 
"goto", 
"jsr", 
"ret", 
"tableswitch", 
"lookupswitch", 
"ireturn", 
"lreturn", 
"freturn", 
"dreturn", 
"areturn", 
"return", 
"getstatic", 
"putstatic", 
"getfield", 
"putfield", 
"invokevirtual", 
"invokespecial", 
"invokestatic", 
"invokeinterface", 
"xxxunusedxxx", 
"new", 
"newarray", 
"anewarray", 
"arraylength", 
"athrow", 
"checkcast", 
"instanceof", 
"monitorenter", 
"monitorexit", 
"wide", 
"multianewarray", 
"ifnull", 
"ifnonnull", 
"goto_w", 
"jsr_w",
"breakpoint", 
"ldc_quick", 
"ldc_w_quick", 
"ldc2_w_quick", 
"getfield_quick", 
"putfield_quick", 
"getfield2_quick", 
"putfield2_quick", 
"getstatic_quick", 
"putstatic_quick",
"getstatic2_quick", 
"putstatic2_quick",
"invokevirtual_quick", 
"invokenonvirtual_quick", 
"invokesuper_quick", 
"invokestatic_quick", 
"invokeinterface_quick",
"invokevirtualobject_quick",
"new_quick",
"anewarray_quick",
"multianewarray_quick",
"checkcast_quick",
"instanceof_quick",
"invokevirtual_quick_w",
"getfield_quick_w", 
"putfield_quick_w",
0L };

enum {
jem_NOP,
jem_ICONST_M1,
jem_ICONST_0,
jem_ICONST_1,
jem_ICONST_2,
jem_ICONST_3,
jem_ICONST_4,
jem_ICONST_5,
jem_BIPUSH,
jem_SIPUSH,
jem_ILOAD,
jem_ILOAD_0,
jem_ILOAD_1,
jem_ILOAD_2,
jem_ILOAD_3,
jem_ISTORE,
jem_ISTORE_0,
jem_ISTORE_1,
jem_ISTORE_2,
jem_ISTORE_3,
jem_IFEQ,
jem_IFNE,
jem_IFLT,
jem_IFLE,
jem_IFGT,
jem_IFGE,
jem_IF_ICMPEQ,
jem_IF_ICMPNE,
jem_IF_ICMPLT,
jem_IF_ICMPGE,
jem_IF_ICMPGT,
jem_IF_ICMPLE,
jem_IF_ACMPEQ,
jem_IF_ACMPNE,

jem_IRETURN,
jem_RETURN,
jem_INVOKEVIRTUAL,
jem_INVOKESTATIC,
jem_GETSTATIC,
jem_PUTSTATIC,

jem_POP,
jem_POP2,
jem_DUP,
jem_DUP_X1,
jem_DUP_X2,
jem_DUP2,
jem_DUP2_X1,
jem_DUP2_X2,
jem_SWAP,
jem_IADD,
jem_ISUB,
jem_INEG,
jem_IOR,
jem_ISHL,
jem_ISHR,
jem_IUSHR,
jem_IAND,
jem_IXOR,
jem_I2B,
jem_GOTO,
jem_INVOKEINTERFACE,
jem_ARRAYLENGTH,
jem_ATHROW,
jem_CHECKCAST,
jem_INSTANCEOF,
jem_WIDE,

jem_IINC,
jem_GETFIELD,
jem_PUTFIELD,
jem_NEW,
jem_NEWARRAY,
jem_ANEWARRAY,
jem_IALOAD,
jem_IASTORE,
jem_BALOAD,
jem_BASTORE,

jem_LDC,
jem_LDC_W,
jem_LDC2_W,
jem_IMUL,
jem_IDIV,
jem_IREM,
jem_JSR,
jem_RET,
jem_TABLESWITCH,
jem_LOOKUPSWITCH,

jem_LAST_OPCODE
};

int gJemOps[] = {
NOP,
ICONST_M1,
ICONST_0,
ICONST_1,
ICONST_2,
ICONST_3,
ICONST_4,
ICONST_5,
BIPUSH,
SIPUSH,
ILOAD,
ILOAD_0,
ILOAD_1,
ILOAD_2,
ILOAD_3,
ISTORE,
ISTORE_0,
ISTORE_1,
ISTORE_2,
ISTORE_3,
IFEQ,
IFNE,
IFLT,
IFLE,
IFGT,
IFGE,
IF_ICMPEQ,
IF_ICMPNE,
IF_ICMPLT,
IF_ICMPGE,
IF_ICMPGT,
IF_ICMPLE,
IF_ACMPEQ,
IF_ACMPNE,

IRETURN,
RETURN,
INVOKEVIRTUAL,
INVOKESTATIC,
GETSTATIC,
PUTSTATIC,

POP,
POP2,
DUP,
DUP_X1,
DUP_X2,
DUP2,
DUP2_X1,
DUP2_X2,
SWAP,
IADD,
ISUB,
INEG,
IOR,
ISHL,
ISHR,
IUSHR,
IAND,
IXOR,
I2B,
GOTO,
INVOKEINTERFACE,
ARRAYLENGTH,
ATHROW,
CHECKCAST,
INSTANCEOF,
WIDE,

IINC,
GETFIELD,
PUTFIELD,
NEW,
NEWARRAY,
ANEWARRAY,
IALOAD,
IASTORE,
BALOAD,
BASTORE,

LDC,
LDC_W,
LDC2_W,
IMUL,
IDIV,
IREM,
JSR,
RET,
TABLESWITCH,
LOOKUPSWITCH,

-1
};

int gJava2JemOps[] = {
jem_NOP,
jem_ICONST_0,
jem_ICONST_M1,
jem_ICONST_0,
jem_ICONST_1,
jem_ICONST_2,
jem_ICONST_3,
jem_ICONST_4,
jem_ICONST_5,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
jem_BIPUSH,
jem_SIPUSH,
jem_LDC,
jem_LDC_W,
jem_LDC2_W,
jem_ILOAD,
-1,
-1,
-1,
jem_ILOAD,
jem_ILOAD_0,
jem_ILOAD_1,
jem_ILOAD_2,
jem_ILOAD_3,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
jem_ILOAD_0,
jem_ILOAD_1,
jem_ILOAD_2,
jem_ILOAD_3,
jem_IALOAD,
-1,
-1,
-1,
jem_IALOAD,
jem_BALOAD,
jem_BALOAD,
jem_IALOAD,
jem_ISTORE,
-1,
-1,
-1,
jem_ISTORE,
jem_ISTORE_0,
jem_ISTORE_1,
jem_ISTORE_2,
jem_ISTORE_3,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
jem_ISTORE_0,
jem_ISTORE_1,
jem_ISTORE_2,
jem_ISTORE_3,
jem_IASTORE,
-1,
-1,
-1,
jem_IASTORE,
jem_BASTORE,
jem_BASTORE,
jem_IASTORE,
jem_POP,
jem_POP2,
jem_DUP,
jem_DUP_X1,
jem_DUP_X2,
jem_DUP2,
jem_DUP2_X1,
jem_DUP2_X2,
jem_SWAP,
jem_IADD,
-1,
-1,
-1,
jem_ISUB,
-1,
-1,
-1,
jem_IMUL,
-1,
-1,
-1,
jem_IDIV,
-1,
-1,
-1,
jem_IREM,
-1,
-1,
-1,
jem_INEG,
-1,
-1,
-1,
jem_ISHL,
-1,
jem_ISHR,
-1,
jem_IUSHR,
-1,
jem_IAND,
-1,
jem_IOR,
-1,
jem_IXOR,
-1,
jem_IINC,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
jem_I2B,
jem_I2B,
jem_NOP,
-1,
-1,
-1,
-1,
-1,
jem_IFEQ,
jem_IFNE,
jem_IFLT,
jem_IFGE,
jem_IFGT,
jem_IFLE,
jem_IF_ICMPEQ,
jem_IF_ICMPNE,
jem_IF_ICMPLT,
jem_IF_ICMPGE,
jem_IF_ICMPGT,
jem_IF_ICMPLE,
jem_IF_ACMPEQ,
jem_IF_ACMPNE,
jem_GOTO,
jem_JSR,
jem_RET,
jem_TABLESWITCH,
jem_LOOKUPSWITCH,
jem_IRETURN,
-1,
-1,
-1,
jem_IRETURN,
jem_RETURN,
jem_GETSTATIC,
jem_PUTSTATIC,
jem_GETFIELD,
jem_PUTFIELD,
jem_INVOKEVIRTUAL,
jem_INVOKESTATIC,
jem_INVOKESTATIC,
jem_INVOKEINTERFACE,
-1,
jem_NEW,
jem_NEWARRAY,
jem_ANEWARRAY,
jem_ARRAYLENGTH,
jem_ATHROW,
jem_CHECKCAST,
jem_INSTANCEOF,
-1,
-1,
jem_WIDE,
-1,   // "multianewarray",    ?? How come this isn't used?
jem_IFEQ,
jem_IFNE,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,             
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1,
-1
 };



