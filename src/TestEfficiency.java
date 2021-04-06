import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import junit.framework.TestCase;
import edu.uwm.cs.util.BitUtil;
import edu.uwm.cs351.HexBoard;
import edu.uwm.cs351.HexCoordinate;
import edu.uwm.cs351.HexTile;
import edu.uwm.cs351.Terrain;


public class TestEfficiency extends TestCase {
	Collection<HexTile> hb;
	Random r;
	
	@Override
	public void setUp() {
		hb = new HexBoard();
		r = new Random();
		try {
			assert r.nextInt(hb.size()) == 42;
			assertTrue(true);
		} catch (IllegalArgumentException ex) {
			System.err.println("You must disable assertions to run this test.");
			System.err.println("Go to Run > Run Configurations. Select the 'Arguments' tab");
			System.err.println("Then remove '-ea' from the VM Arguments box.");
			assertFalse("Assertions must NOT be enabled while running efficiency tests.",true);
		}
	}

	protected final Terrain[] terrains = Terrain.values();
	private static final int MAX_A = 128;
	
	protected HexTile makeHexTile(int i) {
		i = BitUtil.reverseBits(i << 2); // make more suitable for BST
		int t = i % terrains.length;
		int a = i % MAX_A;
		i /= MAX_A;
		return new HexTile(terrains[t],new HexCoordinate(a,i));
	}
		
	
	private static final int MAX_LENGTH = 1000000;
	private static final int SAMPLE = 100;
	
	protected <T> void testLong(Collection<T> col, IntFunction<T> maker, boolean preservesOrder) {
		int sampleIndices[] = new int[SAMPLE];
		Object samples[] = new Object[SAMPLE];

		int j = 0;
		int sam = r.nextInt(MAX_LENGTH/SAMPLE);
		for (int i=0; i < MAX_LENGTH; ++i) {
			T obj = maker.apply(i);
			if (i == sam) {
				sampleIndices[j] = i;
				samples[j] = obj;
				++j;
				if (j < SAMPLE) sam += 1 + r.nextInt((MAX_LENGTH-i)/(SAMPLE-j));
			}
			col.add(obj);
		}

		Iterator<T> it = col.iterator();
		int sum = 0;
		for (j=0; j < SAMPLE; ++j) {
			int n = sampleIndices[j] - sum;
			Object obj = null;
			for (int i=0; i < n; ++i) {
				it.next();
			}
			obj = it.next();
			sum += n;
			++sum;
			if (preservesOrder) assertEquals(samples[j],obj);
		}
	}
	
	public void testALLong() {
		testLong(new ArrayList<Integer>(), (i) -> i, true);
	}
	
	public void testHBLong() {
		testLong(hb,(i) -> makeHexTile(i),false);
		hb.clear();
	}
	

	protected <T> void testStochastic(Supplier<Collection<T>> makeCol, IntFunction<T> maker) {
		List<Collection<T>> ss = new ArrayList<>();
		Collection<T> col = makeCol.get();
		ss.add(col);
		int max = 1;
		for (int i=0; i < MAX_LENGTH; ++i) {
			if (r.nextBoolean()) {
				col = makeCol.get();
				col.add(maker.apply(i));
				ss.add(col);
			} else {
				col.addAll(new ArrayList<>(col)); // double size of s
				if (col.size() > max) {
					max = col.size();
					// System.out.println("Reached " + max);
				}
			}
		}
	}
	
	public void testHBStochastic() {
		testStochastic(() -> new HexBoard(), (i) -> makeHexTile(i));
	}
	
	
	protected <T> void testMiddle(Collection<T> col, IntFunction<T> maker) {
		// this would be too slow with dynamic arrays
		for (int i=0; i < MAX_LENGTH; ++i) {
			col.add(maker.apply(i));
		}
		
		Iterator<T> it = col.iterator();
		int removed = 0;
		for (int i=0; i < MAX_LENGTH; ++i) {
			it.next();
			if (r.nextBoolean()) {
				it.remove();
				++removed;
			}
		}
		
		assertEquals(MAX_LENGTH-removed,col.size());
	}
	
	/* this testuses removal.
	public void testHBMiddle() {
		testMiddle(hb,(i) -> makeHexTile(i));
		hb.clear();
	}
	*/
	
	
	protected <T> void testContains(Collection<T> col, IntFunction<T> maker) {
		ArrayList<T> samples = new ArrayList<>();
		for (int i=0; i < MAX_LENGTH; ++i) {
			T obj = maker.apply(i);
			samples.add(obj);
			col.add(obj);
		}
		for (T obj : samples) {
			assertTrue(col.contains(obj));
		}
	}
	
	public void testHBContains() {
		testContains(hb,(i)->makeHexTile(i));
		hb.clear();
	}
	
	protected <T> void testRemove(Collection<T> col, IntFunction<T> maker) {
		ArrayList<T> samples = new ArrayList<>();
		for (int i=0; i < MAX_LENGTH; ++i) {
			T obj = maker.apply(i);
			if ((i & 1) == 0) samples.add(obj);
			col.add(obj);
		}
		for (T obj : samples) {
			assertTrue(col.remove(obj));
		}
		assertEquals(MAX_LENGTH-samples.size(),col.size());
	}
	
	// Removal not yet implemented
	/*
	public void testHBRemove() {
		testRemove(hb,(i)->makeHexTile(i));
		hb.clear();
	}
	*/
	
	private static final int POWER = 20;
	
	public void testHuge() {
		int max = 1 << (POWER+1);
		for (int p = POWER; p > 0; --p) {
			int diff = (1 << (p+1));
			for (int i=(1 << p); i < max; i += diff) {
				int b = (i >> 3)&(~1);
				int a = (i & 14);
				hb.add(new HexTile(Terrain.LAND,new HexCoordinate(a,b)));
			}
		}
		assertEquals(1<<POWER,hb.size()+1);
		int i = 2;
		for (HexTile t : hb) {
			int b = (i >> 3)&(~1);
			int a = (i & 14);
			assertEquals(new HexCoordinate(a,b),t.getLocation());
			i += 2;
		}
	}
	
	public void testSparse() {
		int big = 1 << POWER;
		HexTile first = new HexTile(Terrain.CITY,new HexCoordinate(0,0));
		HexTile last = new HexTile(Terrain.CITY,new HexCoordinate(big,big));
		hb.add(first);
		hb.add(last);
		for (int i=0; i < big; ++i) {
			Iterator<HexTile> it = hb.iterator();
			assertEquals(first,it.next());
			assertTrue(it.hasNext());
			assertEquals(last,it.next());
		}
	}
}
