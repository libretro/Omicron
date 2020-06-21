package org.github.msx80.omicron.basicutils;

import org.github.msx80.omicron.api.Sys;

/**
 * Utility to draw 2d tile based maps 
 *
 */
public class MapDrawer {

	public static interface MapData
	{
		int getTile(int tx, int ty) throws Exception;
		int getWidth();
		int getHeight();
	}
	
	public final static class MapDataMatrix implements MapData
	{
		private int[][] mapData;
		
		
		public MapDataMatrix(int[][] mapData) {
			this.mapData = mapData;
		}

		@Override
		public int getTile(int tx, int ty) throws Exception {
			
			return mapData[ty][tx];
		}

		@Override
		public int getWidth() {
			
			return mapData[0].length;
		}

		@Override
		public int getHeight() {
			return mapData.length;
		}
		
	}
	
	public final static class MapDataArray implements MapData
	{
		private int[] mapData;
		private int width;
		private int height;
		
		
		
		public MapDataArray(int[] mapData, int width) {
			this.mapData = mapData;
			this.width = width;
			this.height = mapData.length / width;
		}

		@Override
		public int getTile(int tx, int ty) throws Exception {
			return mapData[tx+ ty*width];
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public int getHeight() {
			return height;
		}
		
	}
	
	private Sys sys;
	private int tileWidth;
	private int tileHeight;
	private final MapData map;
	private int tilesPerRowOnSheet;

	public MapDrawer(Sys sys, int tileWidth, int tileHeight, int tilesPerRowOnSheet, int[][] mapData)
	{
		this.sys = sys;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.tilesPerRowOnSheet = tilesPerRowOnSheet;
		this.map = new MapDataMatrix(mapData);
	}
	
	public MapDrawer(Sys sys, int tileWidth, int tileHeight, int tilesPerRowOnSheet, MapData mapData)
	{
		this.sys = sys;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
		this.tilesPerRowOnSheet = tilesPerRowOnSheet;
		this.map = mapData;
	}
	
	/**
	 * draw a portion of the map with a specific tile sheet
	 * @param tileSheetNum a sheet number to take the tilemap from
	 * @param destx pixel coordinate where to put the map on screen
	 * @param desty pixel coordinate where to put the map on screen
	 * @param srctx tile coordinate to start from
	 * @param srcty tile coordinate to start from
	 * @param twidth width of the area to draw, in tiles
	 * @param theight height of the area to draw, in tiles
	 */
	public void draw(int tileSheetNum, int destx, int desty, int srctx, int srcty, int twidth, int theight)
	{
		try {
			for (int y = 0; y < theight; y++) {
				for (int x = 0; x < twidth; x++) {
					
					int c = map.getTile(x+srctx, y+srcty);
					int dx = c % tilesPerRowOnSheet;
					int dy = c / tilesPerRowOnSheet;
					
					sys.draw(tileSheetNum, destx + x*tileWidth, desty + y*tileHeight, dx*tileWidth, dy*tileHeight, tileWidth, tileHeight, 0, 0);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Map drawing error: "+e.getMessage(), e);
		}
	}
}
