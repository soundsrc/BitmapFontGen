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

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;

public class MainWindow {

	protected Shell shlBitmapFontGenerator;
	private FontSheetManager fsm;
	private TabFolder fontSheetTab;
	private Combo fontList;
	private Scale imageViewScale;
	private Label imageViewScaleLabel;
	private Combo imageSize;
	private List fontSizeList;
	private Spinner fontSizeSpinner;
	private Spinner dpiSpinner;
	private Button btnAntialias;
	private Spinner spnBorder;
	private Spinner spnSpacing;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlBitmapFontGenerator.open();
		shlBitmapFontGenerator.layout();
		
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for(String font : e.getAvailableFontFamilyNames()) {
            getFontList().add(font);
        }
        getFontList().select(0);
        
		fsm = new FontSheetManager();

		while (!shlBitmapFontGenerator.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void refreshImageWindow()
	{
		String [] items = getFontSizeList().getItems();
		if(items.length > 0) {
			int [] sizes = new int[items.length];
			int i = 0;
			for(String fontSize : items) {
				sizes[i++] = Integer.parseInt(fontSize);
			}
			fsm.generateFontSheets(this, getFontSheetTab(), getFontList().getText(), sizes, new int[10], Integer.parseInt(getImageSize().getText()),getSpnBorder().getSelection(),getSpnSpacing().getSelection(),btnAntialias.getSelection());
			fsm.setScale(getImageViewScale().getSelection() / 10.0f);
		}
	}
	
	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlBitmapFontGenerator = new Shell();
		shlBitmapFontGenerator.setSize(824, 517);
		shlBitmapFontGenerator.setText("Bitmap Font Generator");
		shlBitmapFontGenerator.setLayout(new GridLayout(3, false));
		
		Composite composite = new Composite(shlBitmapFontGenerator, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		
		Group grpRr = new Group(composite, SWT.NONE);
		grpRr.setText("Font");
		grpRr.setLayout(new GridLayout(1, false));
		
		fontList = new Combo(grpRr, SWT.READ_ONLY);
		fontList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshImageWindow();
			}
		});
		GridData gd_fontList = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_fontList.widthHint = 169;
		fontList.setLayoutData(gd_fontList);
		fontList.select(0);
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Composite composite_1 = new Composite(shlBitmapFontGenerator, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 5));
		
		fontSheetTab = new TabFolder(composite_1, SWT.NONE);
		
		Menu menu = new Menu(shlBitmapFontGenerator, SWT.BAR);
		shlBitmapFontGenerator.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.NONE);
		mntmFile.setText("File");
		
		Group grpOptions = new Group(shlBitmapFontGenerator, SWT.NONE);
		grpOptions.setLayout(new GridLayout(4, false));
		GridData gd_grpOptions = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpOptions.heightHint = 54;
		grpOptions.setLayoutData(gd_grpOptions);
		grpOptions.setText("Options");
		
		Label lblNewLabel = new Label(grpOptions, SWT.NONE);
		lblNewLabel.setText("Border");
		
		spnBorder = new Spinner(grpOptions, SWT.BORDER);
		spnBorder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshImageWindow();
			}
		});
		
		Label lblSpacing = new Label(grpOptions, SWT.NONE);
		lblSpacing.setText("Spacing");
		
		spnSpacing = new Spinner(grpOptions, SWT.BORDER);
		spnSpacing.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshImageWindow();
			}
		});
		
		btnAntialias = new Button(grpOptions, SWT.CHECK);
		btnAntialias.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshImageWindow();
			}
		});
		btnAntialias.setSelection(true);
		btnAntialias.setText("Anti-alias");
		new Label(grpOptions, SWT.NONE);
		new Label(grpOptions, SWT.NONE);
		new Label(grpOptions, SWT.NONE);
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Group grpSpecs = new Group(shlBitmapFontGenerator, SWT.NONE);
		grpSpecs.setLayout(new GridLayout(3, false));
		GridData gd_grpSpecs = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpSpecs.heightHint = 142;
		grpSpecs.setLayoutData(gd_grpSpecs);
		grpSpecs.setText("Output");
		
		fontSizeList = new List(grpSpecs, SWT.BORDER | SWT.V_SCROLL);
		GridData gd_fontSizeList = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_fontSizeList.heightHint = 52;
		gd_fontSizeList.widthHint = 97;
		fontSizeList.setLayoutData(gd_fontSizeList);
		fontSizeList.setItems(new String[] {"24"});
		
		Label lblSize = new Label(grpSpecs, SWT.NONE);
		lblSize.setText("Size");
		
		fontSizeSpinner = new Spinner(grpSpecs, SWT.BORDER);
		fontSizeSpinner.setMaximum(1000);
		fontSizeSpinner.setMinimum(1);
		fontSizeSpinner.setSelection(18);
		
		Composite composite_2 = new Composite(grpSpecs, SWT.NONE);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button button = new Button(composite_2, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int fontSize = Integer.parseInt(getFontSizeSpinner().getText());
				int dpi = Integer.parseInt(getDpiSpinner().getText());
				int pxSize = (int)(fontSize * (dpi / 72.0));
				
				List list = getFontSizeList();
				for(String px : list.getItems()) {
					if(Integer.parseInt(px) == pxSize) return;
				}
				getFontSizeList().add(String.format("%d",pxSize,fontSize,dpi));
				refreshImageWindow();
			}
		});
		button.setText("+");
		
		Button button_1 = new Button(composite_2, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = getFontSizeList().getSelectionIndex();
				if(index >= 0) getFontSizeList().remove(index);
				refreshImageWindow();
			}
		});
		button_1.setText("-");
		
		Label lblDpi = new Label(grpSpecs, SWT.NONE);
		lblDpi.setText("DPI");
		
		dpiSpinner = new Spinner(grpSpecs, SWT.BORDER);
		dpiSpinner.setMaximum(5000);
		dpiSpinner.setMinimum(72);
		dpiSpinner.setSelection(96);
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Group grpImageOut = new Group(shlBitmapFontGenerator, SWT.NONE);
		grpImageOut.setLayout(new RowLayout(SWT.HORIZONTAL));
		grpImageOut.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpImageOut.setText("Image out");
		
		Label lblImageSize = new Label(grpImageOut, SWT.NONE);
		lblImageSize.setText("Image size");
		
		imageSize = new Combo(grpImageOut, SWT.READ_ONLY);
		imageSize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshImageWindow();
			}
		});
		imageSize.setItems(new String[] {"64", "128", "256", "512", "1024", "2048", "4096"});
		imageSize.select(4);
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Group grpAction = new Group(shlBitmapFontGenerator, SWT.NONE);
		grpAction.setText("Action");
		grpAction.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
		
		Button btnGenerate = new Button(grpAction, SWT.NONE);
		btnGenerate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(shlBitmapFontGenerator, SWT.SAVE);
				String file = fileDialog.open();
				if(file != null) {
					FileWriter fp = null;
					try {
						refreshImageWindow();
						
						HashMap<String,BufferedImage> imageMap = new HashMap<String,BufferedImage>(); 
						// write the images
						for(ImageView view : fsm.getImageViews()) {
							BufferedImage image = view.getImage();
							String name = view.getName();
							imageMap.put(name, image);
							ImageIO.write(image,"png",new File(new File(file).getParent(),name + ".png"));
						}
						
						
						// write font specs
						
						fp = new FileWriter(file);
						
						fp.write("{\n");
						
						int spacing = getSpnBorder().getSelection() + getSpnSpacing().getSelection();
						HashMap<Integer,Vector<RectPacker.LayoutTree<FontSheetManager.FontImageInfo>>> layoutObjects = fsm.getLayoutObjects();
						String comma1 = "";
						for(Map.Entry<Integer, Vector<RectPacker.LayoutTree<FontSheetManager.FontImageInfo>>> kv : layoutObjects.entrySet())
						{
							fp.write(comma1);
							String comma2 = "";
							fp.write(String.format("\t\"%d\": {\n",kv.getKey()));
							for(RectPacker.LayoutTree<FontSheetManager.FontImageInfo> layout : kv.getValue()) {
								FontSheetManager.FontImageInfo info = layout.getObject();
								Rectangle r = layout.getRect();

								fp.write(comma2);
								String str = info.character;
								
								BufferedImage image = imageMap.get(info.textureName);
								float scale = (float)image.getWidth();
								
								
								str = str.replace("\\", "\\\\");
								str = str.replace("\"", "\\\"");
								fp.write(String.format("\t\t\"%s\": {\n",str));
								fp.write(String.format("\t\t\t\"texture\": \"%s\",\n",info.textureName));
								fp.write(String.format("\t\t\t\"uv\": [ %f,%f,%f,%f ],\n",
										(r.x + spacing) / scale,
										(image.getHeight() - (r.y + spacing + (r.height - spacing * 2))) / scale,
										(r.x + spacing + (r.width - spacing * 2)) / scale,
										(image.getHeight() - (r.y + spacing)) / scale));
								fp.write(String.format("\t\t\t\"origin\": [ %f,%f ],\n",info.origin.x / scale,info.origin.y / scale));
								fp.write(String.format("\t\t\t\"scale\": %f,\n",scale));
								fp.write(String.format("\t\t\t\"advance\": %f\n",info.advance / scale));
								fp.write("\t\t}");
								comma2 = ",\n";
							}
							fp.write("\n\t}");
							comma1 = ",\n";
						}
						fp.write("\n}\n");
						
						
					} catch(IOException ex) {
						
					} finally {
						try {
							if(fp != null) fp.close();
						} catch(Exception ex) { }
					}
				}
			}
		});
		btnGenerate.setBounds(93, 10, 94, 28);
		btnGenerate.setText("Generate");
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Label label = new Label(shlBitmapFontGenerator, SWT.HORIZONTAL);
		GridData gd_label = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label.widthHint = 290;
		label.setLayoutData(gd_label);
		new Label(shlBitmapFontGenerator, SWT.NONE);
		
		Composite composite_3 = new Composite(shlBitmapFontGenerator, SWT.NONE);
		composite_3.setLayout(new RowLayout(SWT.HORIZONTAL));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		imageViewScaleLabel = new Label(composite_3, SWT.NONE);
		imageViewScaleLabel.setText("100%");
		
		imageViewScale = new Scale(composite_3, SWT.NONE);
		imageViewScale.setPageIncrement(1);
		imageViewScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				imageViewScaleLabel.setText(String.format("%d%%",imageViewScale.getSelection() * 10));
				fsm.setScale(imageViewScale.getSelection() / 10.0f);
			}
		});
		imageViewScale.setMaximum(10);
		imageViewScale.setMinimum(1);
		imageViewScale.setSelection(10);

	}
	public TabFolder getFontSheetTab() {
		return fontSheetTab;
	}
	public Combo getFontList() {
		return fontList;
	}
	public Scale getImageViewScale() {
		return imageViewScale;
	}
	public Label getImageViewScaleLabel() {
		return imageViewScaleLabel;
	}
	public Combo getImageSize() {
		return imageSize;
	}
	public List getFontSizeList() {
		return fontSizeList;
	}
	public Spinner getFontSizeSpinner() {
		return fontSizeSpinner;
	}
	public Spinner getDpiSpinner() {
		return dpiSpinner;
	}
	public Button getBtnAntialias() {
		return btnAntialias;
	}
	public Spinner getSpnBorder() {
		return spnBorder;
	}
	public Spinner getSpnSpacing() {
		return spnSpacing;
	}
}
