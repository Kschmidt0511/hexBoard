package edu.uwm.cs351;

import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import junit.framework.TestCase;

/**
 * An implementation of the HexBoard ADT using 
 * a binary search tree implementation.
 * A hex board is a collection of hex tiles except that there can 
 * never be two tiles at the same location. 
 */
public class HexBoard extends AbstractCollection<HexTile> {

	private static int compare(HexCoordinate h1, HexCoordinate h2) {
		if (h1.b() == h2.b()) {
			return h1.a() - h2.a();
		}
		return h1.b() - h2.b();
	}
	
	private static class Node {
		HexCoordinate loc;
		Terrain terrain;
		Node left, right;
		Node(HexCoordinate l, Terrain t) { loc = l; terrain = t; }
	}
	
	private Node root;
	private int size;
	private int version;
	
	private static boolean doReport = true; 
	private static boolean report(String s) {
		if (doReport) System.err.println("Invariant error: " + s);
		else System.out.println("Detected invariant error: " + s);
		return false;
	}
	
	/**
	 * Return true if the nodes in this BST are properly
	 * ordered with respect to the {@link #compare(HexCoordinate, HexCoordinate)}
	 * method.  If a problem is found, it should be reported (once).
	 * @param r subtree to check (may be null)
	 * @param lo lower bound (if any)
	 * @param hi upper bound (if any)
	 * @return whether there are any problems in the tree.
	 */
	private static boolean isInProperOrder(Node r, HexCoordinate lo, HexCoordinate hi) {
		if (r == null) return true;
		if (r.loc == null) return report("null location in tree");
		if (r.terrain == null) return report("null terrain for " + r.loc);
		if (lo != null && compare(lo,r.loc) >= 0) return report("out of order " + r.loc + " <= " + lo);
		if (hi != null && compare(hi,r.loc) <= 0) return report("out of order " + r.loc + " >= " + hi);
		return isInProperOrder(r.left,lo,r.loc) && isInProperOrder(r.right,r.loc,hi);
	}
	
	/**
	 * Return the count of the nodes in this subtree.
	 * @param p subtree to count nodes for (may be null)
	 * @return number of nodes in the subtree.
	 */
	private static int countNodes(Node p) {
		if (p == null) return 0;
		return 1 + countNodes(p.left) + countNodes(p.right);
	}
	
	private boolean wellFormed() {
		if (!isInProperOrder(root,null,null)) return false;
		int count = countNodes(root);
		if (size != count) return report("size " + size + " wrong, should be " + count);
		return true;
	}
	
	/**
	 * Create an empty hex board.
	 */
	public HexBoard() {
		root = null;
		size = 0;
		assert wellFormed() : "in constructor";
	}
	
	/** Return the terrain at the given coordinate or null
	 * if nothing at this coordinate.
	 * @param c hex coordinate to look for (null OK but pointless)
	 * @return terrain at that coordinate, or null if nothing
	 */
	public Terrain terrainAt(HexCoordinate l) {
		assert wellFormed() : "in terrainAt";
		for (Node p = root; p != null; ) {
			int c = compare(l,p.loc);
			if (c == 0) return p.terrain;
			if (c < 0) p = p.left;
			else p = p.right;
		}
		return null;
	}

	@Override // required by Java
	public Iterator<HexTile> iterator() {
		assert wellFormed() : "in iterator";
		return new MyIterator();
	}

	@Override // required by Java
	public int size() {
		assert wellFormed() : "in size";
		return size;
	}
	
	@Override // required for efficiency
	public boolean contains(Object o) {
		assert wellFormed() : "in contains()";
		if (o instanceof HexTile) {
			HexTile h = (HexTile)o;
			return terrainAt(h.getLocation()) == h.getTerrain();
		}
		return false;
	}

	@Override // required for correctness
	public boolean add(HexTile e) {
		assert wellFormed() : "in add()";
		Node lag = null;
		Node p = root;
		int c = 0;
		while (p != null) {
			c = compare(e.getLocation(),p.loc);
			if (c == 0) break;
			lag = p;
			if (c < 0) p = p.left;
			else p = p.right;
		}
		if (p != null) { // found it!
			if (p.terrain == e.getTerrain()) return false;
			p.terrain = e.getTerrain();
			// size doesn't increase...
		} else {
			p = new Node(e.getLocation(),e.getTerrain());
			++size;
			if (lag == null) root = p;
			else if (c < 0) lag.left = p;
			else lag.right = p;
		}
		++version;
		assert wellFormed() : "after add()";
		return true;
	}

	@Override // more efficient
	public void clear() {
		if (size > 0) {
			root = null;
			size = 0;
			++version;
		}
	}
	
	
	private HexTile predecessorFinder(Node g) {
		
		HexTile pred = null;
		
		//left subtree, all the way right
		g= g.left;
		while(g.right!=null) {
			g = g.right;

		}
	pred = new HexTile(g.terrain, g.loc);		
		
		return pred;

	}
	
	
	private Node removeHelper(Node e, HexTile h) {
		
		//recursive
		
		//base case
		if(e==null)return null;
		
		//go left or right until we get to h
		//once we get to h, do what i did in activity
		if(compare(h.getLocation(), e.loc) >0) {
			e.right =removeHelper(e.right,  h);
			
		}
		
		if(compare(h.getLocation(), e.loc)<0) {
			
		e.left =removeHelper(e.left, h);
		}
			
		if(compare(h.getLocation(),e.loc)==0) {
			if(e.left==null && e.right==null) {
				return null;
				
			}
			if(e.left!=null &&e.right==null) {
				
			return e.left;
				
			}
			if(e.left==null &&e.right!=null) {
				return e.right;
			}
			
			if(e.left!=null && e.right!=null) {
				
				HexTile k = predecessorFinder(e);
				
				e.terrain = k.getTerrain();
				e.loc = k.getLocation();
				
				HexTile eH = new HexTile(e.terrain, e.loc);
				
				e.left= removeHelper(e.left, eH);
				
			}
			
		}
		
		return e;
	}
	@Override
	public boolean remove(Object o) {
		
		if(!(contains(o))) {return false;}
		
		//node has the data of hexTile - which are made of terrain and coordinate
		//cast o to a hextile
		HexTile h = (HexTile)o;
		//connect to tree
		root =removeHelper(root, h);
		
		size--;
		version++;
		
		return true;
	}
	

	
	
	
	
	private class MyIterator implements Iterator<HexTile> {
		// new data structure for iterator:
		private Stack<Node> pending = new Stack<>();
		private HexTile current; // if can be removed
		private int myVersion = version;
		
		//check if whats after current is in right position
		private boolean positionHelper() {
		//find nextGreatestAncestor, and compare it to the node in the iterator
			//isInProperOrder
			//go right 1 step, then recursively go left
			if(compare(current.getLocation(), pending.peek().loc ) <0) {
				return true;}
			return false;
		}
		
		
		private boolean checkerHelperFour() {
			if(positionHelper() ==true) {
			//will find the nextGreatestAncestor
			Node nga =pending.peek();
			return true;	}
			return false;
			
		}
		
		private boolean wellFormed() {
			// TODO:
			// 1. Check the outer invariant (see new syntax in homework description)
			
			if(!(HexBoard.this.wellFormed())){
				return false;
			}
			
			
			// 2. If we are stale, don't check anything else, pretend no problems
			if(myVersion != version) { return true;
			}
			// 3. If current isn't null, there should be a node for it in the tree.
			if(current !=null) {
					if(!(contains(current)==true))
					return report("no node in tree");
				//
			}
			
			
			// 4. If current isn't null, the next node after it should be top of the stack
			// in a stack, the top will be furthest right in a tree
			//helperMethod
			//wanna find nextGreatestAncestor
			//if(current =!null) 
				//if(checkerHelperFour()==true)
						
			//current = pending.pop();
			
				
			
				
				
			// 5. If the stack isn't empty, then it should have all 
			//greater ancestors on top of stack and nothing else.
			Node prev = null;
			//iterate thru stack
			//pull out all items from this container
			for(Node q: pending ) {
				
				if(helpermethod is false)
					return report("idk yet");
				//update previous
				prev =q;
			}
				
			
			//if(pending.size()!=0)
			//order of the stack is from greatest to least. use pending
				//pending.
			return true;
		}

		private boolean fiveHelper(Node r, Node t) {
			//find greatest ancestor, get a pointer
			Node pointer=t;
			
			//can't get an ancestor is t is null
			if(t==null) {
				pointer = root;
			}
			else {
				pointer=t.left;
				
			}
			
			while(pointer != null) {
				
				//check if thing we are comparing is = to r, if so, return true
				if(pointer==r) {
					return true;
				}
				else {
					//keep going right to find r
					pointer = pointer.right;
					
				}
				return false;
			}
			
			
			
			
			
			
			if(compare(r,))
			
		}
		
		private MyIterator(boolean ignored) {} // do not change, and do not use in your code
		
		// TODO: any helper method(s) (see homework description)

		private MyIterator() {
			// TODO
			Node cursor=root;
			current =null;
			while(cursor !=null) {
				pending.push(cursor);
				//after we push it, go left
				cursor = cursor.left;
			}
			assert wellFormed();
		}
		
		@Override // required by Java
		public boolean hasNext() {
			if(!(myVersion == version))
			throw new ConcurrentModificationException("iterator version does not match collection version");
			
			
			//making sure stack is not empty, so a next exists
			
			
			return !(pending.isEmpty()); // TODO
		}

		@Override // required by Java
		public HexTile next() {
			if(!(myVersion == version))
			throw new ConcurrentModificationException("iterator version does not match collection version");
			
			if(!(hasNext()))
				throw new NoSuchElementException("iteration has no more elements");
			
			Node temp= pending.pop();
			
			HexTile tempH = new HexTile(temp.terrain,temp.loc);
			//if(temp.right !=null) 
			temp = temp.right;
			
			while(temp !=null) {
				pending.push(temp);
				//after we push it, go left
				temp = temp.left;}
			
			//make a new hexTile to return
			
			current =tempH;
			
			return tempH; // TODO: find next entry and generate hex tile on demand
		}

		
		
		
		@Override // required for functionality
		//outside remove to help for this remove
		public void remove() {
			if(!(myVersion == version))
			throw new ConcurrentModificationException("iterator version does not match collection version");
		 
			//cant remove a null
			if(current==null)throw new IllegalStateException();
			
			HexBoard.this.remove(current);
			//current is now null
			
			//HexTile newCurrent = new HexTile(current.getTerrain(), current.getLocation());
			current = null;
			//current=pending.pop();
			//increment myVersion
			myVersion++;
		}
	}

	// Do not change anything in this test class:
	public static class TestInternals extends TestCase {
		private HexBoard self;
		private MyIterator it;
		
		private void assertIteratorWellFormed(boolean val) {
			doReport = val;
			assertEquals(val,it.wellFormed());
		}
		
		private HexCoordinate h(int a, int b) {
			return new HexCoordinate(a,b);
		}
		
		private HexTile ht(Terrain t, HexCoordinate h) {
			return new HexTile(t,h);
		}
		
		/**
		 * Return a terrain different than the argument.
		 * @param t terrain, must not be null
		 * @return different terrain (never null)
		 */
		private Terrain not(Terrain t) {
			int i = t.ordinal();
			++i;
			Terrain[] terrains = Terrain.values();
			return terrains[i % terrains.length];
		}
		
		private HexCoordinate h1 = h(3,0), h1x = h(4,0);
		private HexCoordinate h2 = h(2,1);
		private HexCoordinate h3 = h(3,1);
		private HexCoordinate h4 = h(2,2);
		private HexCoordinate h5 = h(3,2);
		private HexCoordinate h6 = h(4,2);
		private HexCoordinate h7 = h(7,4), h7x = h(8,4);
		
		private Node n(HexCoordinate h,Terrain t,Node n1, Node n2) {
			Node result = new Node(h,t);
			result.left = n1;
			result.right = n2;
			return result;
		}
		
		private Node clone(Node n) {
			return n(n.loc,n.terrain,n.left,n.right);
		}
		
		@Override
		protected void setUp() {
			self = new HexBoard();
			self.size = 0;
			self.root = null;
			self.version = 0;
			assertTrue("Main class invariant broken?",self.wellFormed());
			it = self.new MyIterator(false); // special syntax
		}
		
		/**
		 * Set up a tree of the following form
		 * <pre>
		 *       h4
		 *      /  \
		 *    h2    h5
		 *   /  \     \
		 * h1    h3    h7
		 *            /
		 *          h6
		 * </pre>
		 * @param t terrain to start creating nodes with
		 */
		protected void makeMedium(Terrain t) {
			Node a = new Node(h1,t); t = not(t);
			Node c = new Node(h3,t); t = not(t);
			Node b = n(h2,t,a,c); t = not(t);
			Node f = new Node(h6,t); t = not(t);
			Node g = n(h7,t,f,null); t = not(t);
			Node e = n(h5,t,null,g); t = not(t);
			Node d = n(h4,t,b,e);
			self.root = d;
			self.version  = t.hashCode();
			self.size = 7;
			assertTrue("Main class invariant broken?",self.wellFormed());
		}
		
		public void testA() {
			assertIteratorWellFormed(true);
			
			self.size = 8;
			assertIteratorWellFormed(false);
			
			makeMedium(Terrain.CITY);
			it.myVersion = self.version;
			assertIteratorWellFormed(true);
			
			self.size = 8;
			assertIteratorWellFormed(false);
		}
		
		public void testB() {
			++self.version;
			assertIteratorWellFormed(true);
			
			self.size = 8;
			assertIteratorWellFormed(false);
			
			makeMedium(Terrain.LAND);
			it.myVersion = self.version-1;
			assertIteratorWellFormed(true);
			
			self.size = 8;
			assertIteratorWellFormed(false);
		}
		
		public void testC() {
			it.current = ht(Terrain.CITY,h3);
			assertIteratorWellFormed(false);
			
			makeMedium(Terrain.WATER);
			it.myVersion = self.version;
			it.current = null;
			assertIteratorWellFormed(true);
			
			it.current = ht(Terrain.DESERT,h1x);
			assertIteratorWellFormed(false);
			it.current = ht(Terrain.LAND,h7x);
			assertIteratorWellFormed(false);
			it.current = ht(not(self.root.right.right.terrain),self.root.right.right.loc);
			assertIteratorWellFormed(false);
			
			it.current = ht(self.root.right.right.terrain,self.root.right.right.loc);
			assertIteratorWellFormed(true);
		}
		
		public void testD() {
			// Read the Homework assignment on the iterator's well formed
			it.current = ht(Terrain.MOUNTAIN,h4);
			++self.version;
			assertIteratorWellFormed(true);
			
			makeMedium(Terrain.DESERT);
			it.myVersion = self.version-2;
			assertIteratorWellFormed(true);
		}
		
		public void testE() {
			makeMedium(Terrain.INACCESSIBLE);
			it.myVersion = self.version;
			
			it.pending.push(self.root);
			assertIteratorWellFormed(true);
			it.pending.pop();
			
			it.pending.push(self.root.left);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.left.right);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.right);
			assertIteratorWellFormed(true);
			it.pending.pop();
			
			it.pending.push(self.root.right.right);
			assertIteratorWellFormed(true);
			it.pending.pop();
			
			it.pending.push(null);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.right.right.left);
			assertIteratorWellFormed(false);
			it.pending.pop();
		}
		
		public void testF() {
			makeMedium(Terrain.LAND);			
			it.myVersion = self.version;
			
			it.pending.push(self.root);
			assertIteratorWellFormed(true);
			it.current = ht(self.root.left.right.terrain,self.root.left.right.loc);
			assertIteratorWellFormed(true);
			it.current = ht(not(self.root.left.right.terrain),self.root.left.right.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.left.terrain,self.root.left.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.terrain,self.root.loc);
			it.pending.pop();
			
			assertIteratorWellFormed(false);
			
			it.pending.push(self.root.right);
			// it.current still set (see above)
			assertIteratorWellFormed(true);
			it.current = ht(not(self.root.terrain),self.root.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.left.right.terrain,self.root.left.right.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.right.right.terrain,self.root.right.right.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.right.right.left.terrain,self.root.right.right.left.loc);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			assertIteratorWellFormed(false);
			
			it.pending.push(self.root.right.right);
			// it.current still set (see above)
			assertIteratorWellFormed(true);
			it.current = ht(not(self.root.right.right.left.terrain),self.root.right.right.left.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.right.terrain,self.root.right.loc);
			assertIteratorWellFormed(false);
			it.current = null;
			assertIteratorWellFormed(true);
			it.current = ht(self.root.right.right.terrain,self.root.right.right.loc);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			assertIteratorWellFormed(true);
		}
		
		public void testG() {
			makeMedium(Terrain.FOREST);
			it.myVersion = self.version;
			
			Node fakeRoot = clone(self.root);
			it.pending.push(fakeRoot);
			assertIteratorWellFormed(false);
			it.pending.push(fakeRoot.left);
			assertIteratorWellFormed(false);
			
			it.pending.clear();
			it.pending.push(self.root);
			assertIteratorWellFormed(true);
			it.pending.push(clone(self.root.left));
			assertIteratorWellFormed(false);
			it.current = ht(self.root.left.left.terrain,self.root.left.left.loc);
			assertIteratorWellFormed(false);
			
			it.pending.pop();
			it.pending.push(self.root.left);
			assertIteratorWellFormed(true);
			it.current = null;
			assertIteratorWellFormed(true);
		}
		
		public void testH() {
			makeMedium(Terrain.WATER);
			it.myVersion = self.version;
			
			it.pending.push(self.root);
			assertIteratorWellFormed(true);
			
			it.pending.push(self.root);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.right);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.left);
			assertIteratorWellFormed(true);
			it.pending.pop();
		}
		
		public void testI() {
			makeMedium(Terrain.CITY);
			it.myVersion = self.version;
			
			it.pending.push(self.root.left);
			assertIteratorWellFormed(false);
			
			it.pending.push(self.root.left.left);
			assertIteratorWellFormed(false);
			
			it.current = ht(self.root.left.left.terrain,self.root.left.left.loc);
			assertIteratorWellFormed(false);
			
			it.pending.clear();
			it.pending.push(self.root);
			it.pending.push(self.root.left);
			assertIteratorWellFormed(true);
		}
		
		public void testJ() {
			makeMedium(Terrain.MOUNTAIN);
			it.myVersion = self.version;
			
			it.pending.push(self.root);
			
			it.pending.push(self.root.left.left);
			assertIteratorWellFormed(false);
			it.pending.pop();
			
			it.pending.push(self.root.left.right);
			assertIteratorWellFormed(true);
			it.current = ht(self.root.left.left.terrain,self.root.left.left.loc);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.left.terrain,self.root.left.loc);
			assertIteratorWellFormed(true);
			it.pending.pop();
			assertIteratorWellFormed(false);
			
			it.pending.pop();
			assertIteratorWellFormed(false);
		}
		
		public void testK() {
			makeMedium(Terrain.DESERT);
			it.myVersion = self.version;
			
			it.pending.push(self.root);
			it.pending.push(self.root.right);
			assertIteratorWellFormed(false);
			it.current = ht(self.root.terrain,self.root.loc);
			assertIteratorWellFormed(false);
			
			it.pending.clear();
			it.pending.push(self.root.right);
			assertIteratorWellFormed(true);
			it.current = null;
			assertIteratorWellFormed(true);
			
			it.pending.push(self.root.right.right);
			assertIteratorWellFormed(false);
			
			it.pending.clear();
			it.pending.push(self.root.right.right);
			assertIteratorWellFormed(true);
			it.current = ht(self.root.right.terrain,self.root.right.loc);
			assertIteratorWellFormed(false);
			it.pending.push(self.root.right.right.left);
			assertIteratorWellFormed(true);
		}
	}
}
