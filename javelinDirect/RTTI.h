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

#ifndef _RTTI_H_
#define _RTTI_H_

// A unique identifier for each class to
// implement RTTI.
enum eObjectClass {
  kCObject = 0x0101,
  kCMemManager = 0x0201,

  kCType = 0x0301,
  kCInteger,
  kCVarchar,
  kCBoolean,


  kCList = 0x0401,
  kCCollection,
  kCLexiconList,

  kCCursor = 0x0501,
  kCQuery,
  kCQueryCursor,
  kCJavaCursor,
  kCQueryOptimiser,
  kCFuncStack,

  kCDatabase = 0x0601,
  kCDiskFile,
  kCColumn,
  kCTable,
  kCTableIterator,
  kCLexicon,
  kCFieldData,
  kCResult,
  kCRecord,
  kCBlock,

  kCJavaVM = 0x0701,
  kCJavaClass,
  kCQueryEngine,

  kCQueryObject = 0x0801,
  kCQueryName,
  kCQueryDeferredName,
  kCQueryValue,
  kCQueryNode
};

#endif
