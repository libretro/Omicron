package org.github.msx80.omicron.basicutils.gui;

import org.github.msx80.omicron.api.Sys;
import org.github.msx80.omicron.basicutils.Colors;
import org.github.msx80.omicron.basicutils.ShapeDrawer;
import org.github.msx80.omicron.basicutils.palette.Tic80;

public class Container extends ManagedParentWidget {

	private Sys sys;
	
	public Container(Sys sys, int x, int y)
	{
		super(sys, x, y, 0, 0);
		this.sys = sys;
	}
	
	
	@Override
	public void draw() {
		sys.fill(0, 0, 0, w, h, Tic80.BLUE_GRAY);
		ShapeDrawer.outline(sys, 0, 0, w, h, 0, Tic80.BROWN);
		
		drawChildren();
	}

	@Override
	public void childInvalidated(Widget widget) {
		int mw = 0;
		int mh = 0;
		for (Widget w : children()) {
			int cx = w.x + w.w;
			int cy = w.y + w.h;
			if (cx>mw) {
				mw = cx;
			}
			if (cy>mh) {
				mh = cy;
			}
		}
		this.w = mw;
		this.h = mh;
		invalidate();
	}




}
