/*
 Bitmap Font Gen
 Copyright (C) 2012 Sound <sound at sagaforce dot com>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sagaforce.bitmapfontgen;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Vector;

public class RectPacker<T> {
	
	public static class LayoutTree<T>
	{
		private T object;
		private Rectangle rect;
		private LayoutTree<T> left, right;
		
		public LayoutTree(T object, Rectangle rect)
		{
			this.setObject(object);
			this.setRect(rect);
			this.left = null;
			this.right = null;
		}
		
		public LayoutTree(T object, Rectangle rect, LayoutTree<T> left, LayoutTree<T> right)
		{
			this.setObject(object);
			this.setRect(rect);
			this.left = left;
			this.right = right;
		}
		
		public boolean isLeaf()
		{
			return this.left == null && this.right == null;
		}
		
		public LayoutTree<T> insert(T object, Point size)
		{
			if(!isLeaf()) {
				if(this.left != null) {
					LayoutTree<T> ret = this.left.insert(object,size);
					if(ret != null) return ret;
				}
				return this.right.insert(object,size);
			}
			
			if(this.getObject() != null) return null;
			
			Rectangle r = this.getRect();
			int width = size.x;
			int height = size.y;

			// can't fit
			if(width > r.width || height > r.height) return null;
			
			/*
			# split nodes
            #              A
            #     A        +------------+
            #    / \       |     C      |
            #   B   C      +--------+---+
            #  / \         |  D*    | E | B
            # D*  E        +--------+---+

            # or
            #             A
            #             +----+-------+
            #             | E  |       |
            #             +----+   C   |
            #             |    |       |
            #             | D* |       |
            #             +----+-------+
            #               B 
            */
			LayoutTree<T> d = new LayoutTree<T>(object,new Rectangle(r.x,r.y,width,height));
			if(size.x > size.y) {
				LayoutTree<T> e = new LayoutTree<T>(null,new Rectangle(r.x + width,r.y,r.width - width,height));
				LayoutTree<T> b = new LayoutTree<T>(null,new Rectangle(r.x,r.y,r.width,height),d,e);
				LayoutTree<T> c = new LayoutTree<T>(null,new Rectangle(r.x,r.y + height,r.width,r.height - height));
				this.left = b;
				this.right = c;
			} else {
				LayoutTree<T> e = new LayoutTree<T>(null,new Rectangle(r.x,r.y + height,width,r.height - height));
				LayoutTree<T> b = new LayoutTree<T>(null,new Rectangle(r.x,r.y,width,r.height),d,e);
				LayoutTree<T> c = new LayoutTree<T>(null,new Rectangle(r.x + width,r.y,r.width - width,r.height));
				this.left = b;
				this.right = c;
			}
			
			return d;
		}

		public T getObject() {
			return object;
		}

		public void setObject(T object) {
			this.object = object;
		}

		public Rectangle getRect() {
			return rect;
		}

		public void setRect(Rectangle rect) {
			this.rect = rect;
		}
	};
	
	public static class RectObject<T> implements Comparable<RectObject<T>>
	{
		private Point size;
		private T object;

		public RectObject(T object,Point size)
		{
			this.object = object;
			this.size = size;
		}

		@Override
		public int compareTo(RectObject<T> other) {
			// TODO Auto-generated method stub
			return other.size.x * other.size.y - this.size.x * this.size.y;
		}

		public T getObject() {
			return object;
		}
		
		public Point getSize() {
			return size;
		}
	};


	public RectPacker()
	{
	}

	public boolean layout(Vector<RectObject<T>> objList,Point size,Vector<LayoutTree<T>> outTree, Vector<RectObject<T>> overflow)
	{
		Vector<RectObject<T>> objListCopy = (Vector<RectObject<T>>)objList.clone();
		Collections.sort(objListCopy);

		LayoutTree<T> root = new LayoutTree<T>(null, new Rectangle(0,0,size.x,size.y));
		boolean overflowed = false;
		for(RectObject<T> object : objListCopy) {
			LayoutTree<T> layedOut = root.insert(object.getObject(),object.getSize());
			if(layedOut != null) {
				if(outTree != null) outTree.add(layedOut);
			} else {
				overflowed = true;
				if(overflow != null) overflow.add(object);
			}
		}
		
		return !overflowed;
	}
}
