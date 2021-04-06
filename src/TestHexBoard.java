import java.util.Iterator;

import edu.uwm.cs351.HexBoard;
import edu.uwm.cs351.HexCoordinate;
import edu.uwm.cs351.HexTile;
import edu.uwm.cs351.Terrain;


public class TestHexBoard extends TestCollection<HexTile> {

	private HexBoard hb;
	
	protected HexCoordinate h(int a, int b) { return new HexCoordinate(a,b); }
	
	@Override
	protected void initCollections() {
		c = hb = new HexBoard();
		e = new HexTile[]{
				new HexTile(Terrain.INACCESSIBLE,h(0,0)),
				new HexTile(Terrain.LAND,h(1,0)),
				new HexTile(Terrain.WATER,h(2,0)),
				new HexTile(Terrain.FOREST,h(3,1)),
				new HexTile(Terrain.MOUNTAIN,h(4,2)),
				new HexTile(Terrain.DESERT,h(5,3)),
				new HexTile(Terrain.CITY,h(6,3)),
				new HexTile(Terrain.LAND,h(7,3)),
				new HexTile(Terrain.WATER,h(8,4)),
				new HexTile(Terrain.FOREST,h(9,4)),
				new HexTile(Terrain.MOUNTAIN,h(0,5)),
				new HexTile(Terrain.DESERT,h(1,6)),
				new HexTile(Terrain.CITY,h(2,6)),
				new HexTile(Terrain.LAND,h(3,7))};
		permitNulls = false;
		preserveOrder = false;
		permitDuplicates = false;
		// failFast = false;
		// hasRemove = false;
	}

	
	/// Locked tests
	
	public void test() {
		HexTile forest32 = new HexTile(Terrain.FOREST,h(3,2));
		HexTile city32 = new HexTile(Terrain.CITY,h(3,2));
		HexTile forest23 = new HexTile(Terrain.FOREST,h(2,3));
		// HexBoard hb starts empty
		hb.add(forest23);
		hb.add(forest32);
		Iterator<HexTile> it = hb.iterator();
		// TERRAIN<a,b,c>, true/false, or an exception
		assertEquals(Ts(1957987135),toString(() -> it.next()));
		assertEquals(Ts(578524790),toString(() -> hb.remove(null)));
		assertEquals(Ts(1425022936),toString(() -> hb.remove(city32)));
		assertEquals(Ts(1779272750),toString(() -> it.next()));
		assertEquals(Ts(1374770367),toString(() -> hb.remove(forest32)));
		assertEquals(Ts(506281216),toString(() -> it.next()));
	}
	
	
	/// some spot checks:
	
	public void testA() {
		assertException(NullPointerException.class, () -> hb.add(null));
	}
	
	public void testB() {
		HexTile t = new HexTile(Terrain.LAND,h(0,0));
		assertTrue(hb.add(t));
		assertFalse(hb.add(t));
	}
	
	public void testC() {
		HexTile t = new HexTile(Terrain.LAND,h(0,0));
		hb.add(e[0]);
		assertTrue(hb.add(t));
		assertEquals(1,hb.size());
		assertTrue(hb.contains(t));
	}
	
	public void testD() {
		HexTile t = new HexTile(Terrain.CITY,h(5,3));
		hb.add(t);
		hb.add(e[5]);
		assertEquals(1,hb.size());
	}
	
	public void testE() {
		hb.add(e[3]);
		hb.add(e[4]);
		hb.add(e[5]);
		assertEquals(Terrain.FOREST,hb.terrainAt(h(3,1)));
	}
	
	public void testF() {
		hb.add(e[6]);
		assertFalse(hb.contains(new HexTile(Terrain.MOUNTAIN,h(6,2))));
	}
	
	public void testG() {
		HexTile t = new HexTile(Terrain.CITY,h(5,3));
		hb.add(e[5]);
		assertFalse(hb.contains(t));
		assertFalse(hb.remove(t));
		assertTrue(hb.contains(e[5]));
	}
	
	public void testH() {
		hb.add(e[1]);
		hb.add(e[2]);
		hb.add(new HexTile(Terrain.CITY,e[2].getLocation()));
		assertEquals(Terrain.CITY,hb.terrainAt(e[2].getLocation()));
		assertEquals(null,hb.terrainAt(e[3].getLocation()));
	}
	
	private static final int BIG = 13;
	
    private void makeBigTree() {
        // (((1)2(((3(4))5)6((7(8))9)))10((11)12(13)))
    	hb.add(e[10]);
		hb.add(e[6]);
        hb.add(e[2]);
        hb.add(e[1]);
        hb.add(e[10]);
        hb.add(e[5]);
        hb.add(e[3]);
        hb.add(e[4]);
        hb.add(e[9]);
        hb.add(e[7]);
        hb.add(e[8]);
        hb.add(e[12]);
        hb.add(e[11]);
        hb.add(e[13]);
    }


    /// intense testing of remove
    
	public void testI() {
		for (int i=1; i <= BIG; ++i) {
			makeBigTree();
			assertTrue("Removing e[" + i + "]",hb.remove(e[i]));
			assertFalse("Removed e[" + i + "]",hb.contains(e[i]));
			assertEquals(BIG-1,hb.size());
			hb.clear();
		}
	}
	
	public void testJ() {
		for (int i=1; i <= BIG; ++i) {
			makeBigTree();
			it = hb.iterator();
			for (int j=1; j <= BIG; ++j) {
				assertEquals("While removing e[" + i + "], checking e[" + j + "]",e[j],it.next());
				if (i == j) it.remove();
			}
			assertFalse("Removed e[" + i + "]",hb.contains(e[i]));
			assertEquals(BIG-1,hb.size());
			hb.clear();
		}
	}
	
	public void testK() {
		makeBigTree();
		it = hb.iterator();
		for (int j=1; j <= BIG; ++j) {
			assertEquals("While removing ODD, checking e[" + j + "]",e[j],it.next());
			if ((j%2) == 1) it.remove();
		}
		assertEquals(BIG/2,hb.size());
	}
	
	public void testL() {
		makeBigTree();
		for (int j=1; j <= BIG; ++j) {
			assertTrue("Removing e[" + j + "]",hb.remove(e[j]));
		}
		assertTrue(hb.isEmpty());
	}
	
	
	/// testing iterator using sorting order
	
	public void test90() {
		hb.add(new HexTile(Terrain.CITY,h(3,1)));
		hb.add(new HexTile(Terrain.CITY,h(5,0)));
		it = hb.iterator();
		assertEquals(h(5,0),it.next().getLocation());
	}
	
	public void test91() {
		hb.add(new HexTile(Terrain.CITY,h(3,2)));
		hb.add(new HexTile(Terrain.CITY,h(2,2)));
		it = hb.iterator();
		assertEquals(h(2,2),it.next().getLocation());
	}
	
	public void test92() {
		hb.add(new HexTile(Terrain.CITY,h(3,2)));
		hb.add(new HexTile(Terrain.CITY,h(2,2)));
		it = hb.iterator();
		it.next();
		assertEquals(h(3,2),it.next().getLocation());		
	}
	
	public void test93() {
		hb.add(new HexTile(Terrain.CITY,h(3,2)));
		hb.add(new HexTile(Terrain.CITY,h(5,2)));
		it = hb.iterator();
		assertEquals(h(3,2),it.next().getLocation());		
		assertEquals(h(5,2),it.next().getLocation());
	}
	
	public void test94() {
		hb.add(new HexTile(Terrain.CITY,h(3,2)));
		hb.add(new HexTile(Terrain.CITY,h(5,2)));
		hb.add(new HexTile(Terrain.CITY,h(2,2)));
		it = hb.iterator();
		assertEquals(h(2,2),it.next().getLocation());
		assertEquals(h(3,2),it.next().getLocation());		
		assertEquals(h(5,2),it.next().getLocation());
	}
	
	public void test95() {
		hb.add(new HexTile(Terrain.CITY,h(4,0)));
		hb.add(new HexTile(Terrain.CITY,h(3,2)));
		hb.add(new HexTile(Terrain.CITY,h(5,-1)));
		hb.add(new HexTile(Terrain.CITY,h(10,2)));
		hb.add(new HexTile(Terrain.CITY,h(5,5)));
		it = hb.iterator();
		assertEquals(h(5,-1),it.next().getLocation());
		assertEquals(h(4,0),it.next().getLocation());		
		assertEquals(h(3,2),it.next().getLocation());
		assertEquals(h(10,2),it.next().getLocation());		
		assertEquals(h(5,5),it.next().getLocation());
	}
	
	public void test96() {
		
	}
}
