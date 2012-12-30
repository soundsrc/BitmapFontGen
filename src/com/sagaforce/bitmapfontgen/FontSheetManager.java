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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


public class FontSheetManager {

	private Vector<BufferedImage> mImageSheets = new Vector<BufferedImage>();
	private HashMap<Integer,Vector<RectPacker.LayoutTree<FontImageInfo>>> mLayoutObjects = new HashMap<Integer,Vector<RectPacker.LayoutTree<FontImageInfo>>>();
	private Vector<ImageView> mImageViews = new Vector<ImageView>();
	private Vector<TabItem> mTabs = new Vector<TabItem>();
	
	public class FontImageInfo
	{
		public String textureName;
		public String character;
		public BufferedImage image;
		public Point origin;
		public int ptSize;
		public float advance;
	};
	
	private Rectangle trimImage(BufferedImage image)
	{
		int minX = image.getWidth(), minY = image.getHeight(), maxX = 0, maxY = 0;
 
		for(int y = 0; y < image.getHeight(); ++y) {
			for(int x = 0; x < image.getWidth(); ++x) {
				int alpha = (image.getRGB(x, y) >> 24) & 0xFF;
				if(alpha > 0) {
					if(x <= minX) minX = x;
					if(x >= maxX) maxX = x + 1;
					if(y <= minY) minY = y;
					if(y >= maxY) maxY = y + 1;
				}
			}
		}
		
		return new Rectangle(minX,minY,maxX - minX,maxY - minY);
	}

	public void generateFontSheets(MainWindow main, TabFolder tab,String font,int pxSizes[],int characters[],int maxImageSize,int border,int spacing,boolean antialias)
	{
		dispose();
		
		characters = new int[128-32];
		for(int i = 32; i < 128; ++i) characters[i-32] = i;
		
		int imageSize;
		for(int size : pxSizes) {
			int sheetNum = 0;

			imageSize = maxImageSize;

			RectPacker<FontImageInfo> packer = new RectPacker<FontImageInfo>();
			Vector<RectPacker.RectObject<FontImageInfo>> objList = new Vector<RectPacker.RectObject<FontImageInfo>>();

			for(int ch : characters) {
				FontImageInfo fii = new FontImageInfo();
				
				Font f = new Font(font, Font.PLAIN, size);

				//gc.setAlpha(255);
				BufferedImage img = new BufferedImage(size * 2,size * 2,BufferedImage.TYPE_4BYTE_ABGR);
				Graphics gc = img.getGraphics();

				gc.setFont(f);

				FontMetrics fm = gc.getFontMetrics();
	
				String c = String.valueOf((char)ch);

				LineMetrics lm = fm.getLineMetrics(c,0,1,gc);

				int ascent = (int)Math.ceil(lm.getAscent());
				int descent = (int)Math.ceil(lm.getDescent());
				int height = ascent + descent;
				int width = (int)Math.ceil(fm.charWidth(ch));
				
				//gc.setTextAntialias(antialias ? SWT.ON : SWT.OFF);

				((Graphics2D)gc).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antialias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
				
				gc.setColor(new Color(255,255,255));
				gc.drawString(c, 0, ascent);

				if(width == 0) width = 1;
				if(height == 0) height = 1;
				
				fii.character = c;
				fii.image = new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
				fii.origin = new Point(0,fm.getDescent());
				fii.ptSize = size;
				fii.advance = fm.charWidth(ch);

				fii.image.getGraphics().drawImage(img,0,0,null);
				
				Rectangle trimRect = trimImage(fii.image);
				if(trimRect.width > 0 && trimRect.height > 0) {
					BufferedImage trimmedImage = new BufferedImage(trimRect.width,trimRect.height,BufferedImage.TYPE_4BYTE_ABGR);
					trimmedImage.getGraphics().drawImage(fii.image, 0, 0, trimRect.width, trimRect.height, trimRect.x, trimRect.y, trimRect.x + trimRect.width, trimRect.y + trimRect.height, null);
					fii.origin.x -= trimRect.x;
					fii.origin.y -= height - (trimRect.y + trimRect.height);
					fii.image = trimmedImage;
					width = trimRect.width;
					height = trimRect.height;
				}

				objList.add(new RectPacker.RectObject<FontImageInfo>(fii,new Point(width + 2 * (border + spacing),height + 2 * (border + spacing))));
			}
		
			Vector<RectPacker.RectObject<FontImageInfo>> overflow = objList;
			do {
				objList = overflow;
				Vector<RectPacker.LayoutTree<FontImageInfo>> layoutObjects = new Vector<RectPacker.LayoutTree<FontImageInfo>>();
				
				overflow = new Vector<RectPacker.RectObject<FontImageInfo>>();
				
				do {
					if(!packer.layout(objList, new Point(imageSize >> 1,imageSize >> 1), null, null)) break;
					imageSize >>= 1;
				} while(imageSize >= 256);
				
				packer.layout(objList, new Point(imageSize,imageSize), layoutObjects, overflow);
				
				BufferedImage image = new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_4BYTE_ABGR);
				Graphics gc = image.getGraphics();
				
				//gc.setColor(new Color(0,0,0,255));
				//gc.fillRect(0, 0, image.getWidth(), image.getHeight());
				
				String textureName = String.format("%s_%dpx_%d",font.replace(" ", "_"),size,sheetNum++);
				for(RectPacker.LayoutTree<FontImageInfo> rect : layoutObjects)
				{
					gc.drawImage(rect.getObject().image, rect.getRect().x + border + spacing, rect.getRect().y + border + spacing,null);

					rect.getObject().textureName = textureName;
					Vector<RectPacker.LayoutTree<FontImageInfo>> lo = getLayoutObjects().get(rect.getObject().ptSize);
					if(lo == null) {
						lo = new Vector<RectPacker.LayoutTree<FontImageInfo>>();
						getLayoutObjects().put(rect.getObject().ptSize,lo);
					}
					lo.add(rect);
				}
			
				ImageView view = new ImageView(tab, main, textureName, image, layoutObjects, SWT.NONE);
				TabItem tabItem = new TabItem(tab, SWT.NONE);
				tabItem.setText(textureName);
				tabItem.setControl(view);
		
				getImageViews().add(view);
				getImageSheets().add(image);
				mTabs.add(tabItem);
			} while(!overflow.isEmpty());
		}
	}
	
	public void setScale(float scale)
	{
		for(ImageView view : getImageViews()) {
			view.setScale(scale);
		}
	}
	
	public void dispose()
	{
		for(ImageView view : getImageViews()) {
			view.dispose();
		}
		for(TabItem tabs : mTabs) {
			tabs.dispose();
		}
		
		mLayoutObjects = new HashMap<Integer,Vector<RectPacker.LayoutTree<FontImageInfo>>>(); 
		getImageViews().clear();
		getImageSheets().clear();
		mTabs.clear();
	}
	
	public int numSheets()
	{
		return getImageSheets().size();
	}
	
	public BufferedImage getImage(int index)
	{
		return getImageSheets().get(index);
	}

	public HashMap<Integer,Vector<RectPacker.LayoutTree<FontImageInfo>>> getLayoutObjects() {
		return mLayoutObjects;
	}

	public Vector<BufferedImage> getImageSheets() {
		return mImageSheets;
	}

	public Vector<ImageView> getImageViews() {
		return mImageViews;
	}

}
