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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.sagaforce.bitmapfontgen.FontSheetManager.FontImageInfo;

public class ImageView extends Composite {

	private String mName;
	private Image mSwtImage;
	private BufferedImage mImage;
	private float mScale;
	private Composite canvas;
	private ScrolledComposite scrolledComposite;
	private Vector<RectPacker.LayoutTree<FontImageInfo>> mLayoutObjects;
	private MainWindow mMainWindow;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ImageView(Composite parent, MainWindow main, String name, BufferedImage image, Vector<RectPacker.LayoutTree<FontImageInfo>> layoutObjects, int style) {
		super(parent, style);
		
		mMainWindow = main;
		mName = name;
		mImage = image;
		mLayoutObjects = layoutObjects;

		byte [] data = ((DataBufferByte)image.getData().getDataBuffer()).getData();
		
		PaletteData palette = new PaletteData(0xFF,0xFF00,0xFF0000);
		ImageData imgData = new ImageData(image.getWidth(),image.getHeight(),32,palette,4,data);
		//imgData.alpha = -1;
		imgData.alphaData = new byte[image.getWidth() * image.getHeight()];
		for(int i = 0; i < image.getWidth() * image.getHeight(); ++i) {
			imgData.alphaData[i] = data[i * 4];
		}
		mSwtImage = new Image(this.getDisplay(),imgData);
		
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setAlwaysShowScrollBars(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		canvas = new Composite(scrolledComposite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		scrolledComposite.setContent(canvas);
		
//		Frame frame = SWT_AWT.new_Frame(canvas);
//		frame.add(new JPanel() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = -799688210283491907L;
//
//			public void paint(Graphics g)
//			{
//				super.paint(g);
//				
//				int maxW = mImage.getWidth();
//				int maxH = mImage.getHeight();
//				g.setColor(new Color(255,255,255));
//				g.fillRect(0, 0, (int)(maxW * mScale), (int)(maxH * mScale));
//				g.drawImage(mImage, 0, 0, maxW, maxH, 0, 0, (int)(maxW * mScale), (int)(maxH * mScale), null);
//			}
//		});
		
		setScale(1.0f);
		
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int maxW = mSwtImage.getBounds().width - e.x;
				int maxH = mSwtImage.getBounds().height - e.y;
				
				Color c = new Color(gc.getDevice(),0,0,0);
				gc.setBackground(c);
				gc.fillRectangle(e.x, e.y, maxW, maxH);
				c.dispose();

				gc.drawImage(mSwtImage, e.x, e.y, maxW, maxH, (int)(e.x * mScale), (int)(e.y * mScale), (int)(maxW * mScale), (int)(maxH * mScale));
				
				int spacing = mMainWindow.getSpnBorder().getSelection() + mMainWindow.getSpnSpacing().getSelection();
				for(RectPacker.LayoutTree<FontImageInfo> rect : mLayoutObjects)
				{
					Color c1;
					// draw red rect
					c1 = new Color(gc.getDevice(),192,0,0);
					gc.setForeground(c1);					
					gc.drawRectangle((int)(rect.getRect().x * mScale),(int)(rect.getRect().y * mScale),(int)(rect.getRect().width - 1 * mScale),(int)(rect.getRect().height - 1 * mScale));
					c1.dispose();
					
					// draw origin?
					c1 = new Color(gc.getDevice(),0,0,255);
					gc.setBackground(c1);
					gc.fillOval((int)((rect.getObject().origin.x + rect.getRect().x - 2 + spacing) * mScale),(int)((rect.getRect().height - rect.getObject().origin.y + rect.getRect().y - 2 - spacing) * mScale),4,4);
					c1.dispose();
				}
			}
		});
	}

	public void setScale(float scale)
	{
		mScale = scale;
		Point size = new Point((int)(mSwtImage.getBounds().width * mScale), (int)(mSwtImage.getBounds().height * mScale));
		getCanvas().setSize(size);
		getScrolledComposite().setMinSize(size);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	public Composite getCanvas() {
		return canvas;
	}
	public ScrolledComposite getScrolledComposite() {
		return scrolledComposite;
	}

	public BufferedImage getImage() {
		return mImage;
	}
	
	public String getName() {
		return mName;
	}
}
