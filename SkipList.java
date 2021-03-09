// Joel Joy
// COP 3503
// Spring 2021
// jo161289

import java.io.*;
import java.util.*;
import java.lang.Math;


class Node<AnyType extends Comparable<AnyType>>
{
	ArrayList<Node<AnyType>> nextPointers = new ArrayList<Node<AnyType>>();
	AnyType data;
	int height;

	Node(int height)
	{
		this.height = height;
		for(int i = 0; i < height; i++)
			this.nextPointers.add(null);
	}

	Node(AnyType data, int height)
	{
		this.data = data;
		this.height = height;
		for(int i = 0; i < height; i++)
			this.nextPointers.add(null);
	}

	public AnyType value()
	{
		return this.data;
	}

	public int height()
	{
		return this.height;
	}

	public Node<AnyType> next(int level)
	{
		if (level > this.height - 1 || level < 0)
			return null;
		else
			return this.nextPointers.get(level);
	}

	public void setNext(int level, Node<AnyType> node)
	{
		this.nextPointers.set(level, node);
	}
	// Grow a node
	public void grow()
	{
		this.nextPointers.add(null);
		this.height++;
	}
	// Randomely grow a node half the time
	public boolean maybeGrow()
	{
		if ((int)(Math.random() * 2) == 1)
		{
			this.nextPointers.add(null);
			this.height++;
			return true;
		}
		return false;
	}
	// Trim a node until the height becomes the value passed in
	public void trim(int height)
	{
		while (this.height > height)
		{
			if (this.height > 0)
			{
				this.nextPointers.remove(this.height-1);
				this.height--;
			}
		}
	}
}

public class SkipList<AnyType extends Comparable<AnyType>>
{
	int MAX_HEIGHT = 1;
	int size = 0;
	Node<AnyType> head;
	// Initialize a head node with height 1
	SkipList()
	{
		head = new Node<AnyType>(MAX_HEIGHT);
	}
	// Initialize a head node of height that is passed in
	SkipList(int height)
	{
		if (height < 1)
		{
			head = new Node<AnyType>(MAX_HEIGHT);
		}
		else
		{ 
			head = new Node<AnyType>(height);
			MAX_HEIGHT = height;
		}
	}

	public int size()
	{
		return size;
	}

	public int height()
	{
		return head.height;
	}

	public Node<AnyType> head()
	{
		return head;
	}
	// Inserts a new node into the skiplist
	public void insert(AnyType data)
	{
		int randHeight = generateRandomHeight(MAX_HEIGHT);
		// Create a new node of random height within the maximum allowable height
		Node<AnyType> newNode = new Node<AnyType>(data,randHeight);
		if (size == 0)
		{
			// Inserts the first node into the skiplist
			for (int i = 0; i <= randHeight-1; i++)
				head.setNext(i, newNode);
			size++;
		}
		else
		{
			// Inserts subsequent nodes after the first one into the skiplist
			int level = MAX_HEIGHT - 1;
			Node<AnyType> curr = head;
			// Keeps two arraylists to record where the new node needs to be connected
			ArrayList<Node<AnyType>> newNodePointers = new ArrayList<Node<AnyType>>();
			ArrayList<Integer> pointerLevels = new ArrayList<Integer>();
			while (level >= 0 && curr != null)
			{
				// If the next node on this level is less than data, go to that node
				if (curr.next(level) != null && curr.next(level).data.compareTo(data) < 0)
				{
					curr = curr.next(level);
				}
				else
				{
					newNodePointers.add(curr);
					pointerLevels.add(level);
					level--;
				}
			}
			for (int i = 0; i < newNodePointers.size(); i++)
			{
				if (pointerLevels.get(i) <= newNode.height - 1)
				{
					newNode.setNext(pointerLevels.get(i),newNodePointers.get(i).next(pointerLevels.get(i)));
					newNodePointers.get(i).setNext(pointerLevels.get(i), newNode);
				}
			}
			size++;
			
		}
		// Grow the skiplist if the size is high enough
		int newHeight = getMaxHeight(size);
		if (newHeight > MAX_HEIGHT)
		{
			growSkipList();
			MAX_HEIGHT = newHeight;
		}
	}

	public void insert(AnyType data, int height)
	{
		// Create a new node of given height
		Node<AnyType> newNode = new Node<AnyType>(data,height);
		if (size == 0)
		{
			// Inserts the first node into the skiplist
			head.setNext(0, newNode);
			for (int i = 1; i < height; i++)
			{
				if (head.height < height)
					head.grow();
				head.setNext(i, newNode);
			}
			size++;
		}
		else
		{
			// Inserts subsequent nodes after the first one into the skiplist
			int level = MAX_HEIGHT - 1;
			Node<AnyType> curr = head;
			// Keeps two arraylists to record where the new node needs to be connected
			ArrayList<Node<AnyType>> newNodePointers = new ArrayList<Node<AnyType>>();
			ArrayList<Integer> pointerLevels = new ArrayList<Integer>();
			while (level >= 0 && curr != null)
			{
				// If the next node on this level is less than data, go to that node
				if (curr.next(level) != null && curr.next(level).data.compareTo(data) < 0)
				{
					curr = curr.next(level);
				}
				else
				{
					newNodePointers.add(curr);
					pointerLevels.add(level);
					level--;
				}
			}
			for (int i = 0; i < newNodePointers.size(); i++)
			{
				if (pointerLevels.get(i) <= newNode.height - 1)
				{
					newNode.setNext(pointerLevels.get(i),newNodePointers.get(i).next(pointerLevels.get(i)));
					newNodePointers.get(i).setNext(pointerLevels.get(i), newNode);
				}
				
			}
			size++;
		}
		// Grow the skiplist if the given height is higher than current max height or if the size is high enough
		if (height > MAX_HEIGHT)
		{
			MAX_HEIGHT = height;
			growSkipList();
		}
		else if (size > 1)
		{
			if (MAX_HEIGHT < getMaxHeight(size))
			{
				growSkipList();
				MAX_HEIGHT = getMaxHeight(size);
			}
		}
	}
	
	public void delete(AnyType data)
	{
		int level = MAX_HEIGHT - 1;
		Node<AnyType> curr = head;
		Node<AnyType> delNode = null;
		// Keeps two arraylists to record where to connect nodes after the designated node is deleted
		ArrayList<Node<AnyType>> newNodePointers = new ArrayList<Node<AnyType>>();
		ArrayList<Integer> pointerLevels = new ArrayList<Integer>();
		while (level >= 0 && curr != null)
		{
			// If the next node on this level is less than data, go to that node
			if (curr.next(level) != null && curr.next(level).data.compareTo(data) < 0)
			{
				curr = curr.next(level);
			}
			else if (curr.next(level) != null && curr.next(level).data.compareTo(data) == 0)
			{
				newNodePointers.add(curr);
				pointerLevels.add(level);
				delNode = curr.next(level);
				level--;
			}
			else
			{
				newNodePointers.add(curr);
				pointerLevels.add(level);
				level--;
			}
		}
		// If the given data is in the skiplist, delNode will not be null and will initiate the deletion process
		if (delNode != null)
		{
			for (int i = 0; i < newNodePointers.size(); i++)
			{
				if (pointerLevels.get(i) <= delNode.height - 1)
				{
					newNodePointers.get(i).setNext(pointerLevels.get(i), delNode.next(pointerLevels.get(i)));
				}
			}
			size--;
			int newHeight = getMaxHeight(size);
			while (newHeight < MAX_HEIGHT && size != 1)
			{
				trimSkipList();
				MAX_HEIGHT--;
			}
		}
		
	}
	// Returns true if the data is present in the skiplist, return false otherwise
	public boolean contains(AnyType data)
	{
		int level = MAX_HEIGHT - 1;
		Node<AnyType> curr = head;
		Node<AnyType> temp;
		int compare = 0;
		// Iterates through the skiplist while level is not less than 0 to look for data
		// Uses the same process as insert method to find target node
		while (level >= 0 && curr != null)
		{
			temp = curr.next(level);
			if (temp != null)
			{
				compare = temp.data.compareTo(data);
			}
			if (temp != null && compare < 0)
				curr = curr.next(level);
			else if (temp != null && compare == 0)
				return true;
			else
				level--;
		}
		return false;
	}
	// Returns a pointer to the node that contains data
	public Node<AnyType> get(AnyType data)
	{
		int level = MAX_HEIGHT - 1;
		Node<AnyType> curr = head;
		Node<AnyType> temp;
		int compare = 0;
		// Iterates through the skiplist while level is not less than 0 to look for the node containing data
		// Uses the same process as insert method to find target node
		while (level >= 0 && curr != null)
		{
			temp = curr.next(level);
			if (temp != null)
			{
				compare = temp.data.compareTo(data);
			}
			if (temp != null && compare < 0)
				curr = curr.next(level);
			else if (temp != null && compare == 0)
				return curr;
			else
				level--;
		}
		return null;
	}
	// Return a random value less than or equal to the value passed in
	private static int generateRandomHeight(int maxHeight)
	{
		int height = 1;
		while ((int)(Math.random() * 2) == 1 && height < maxHeight)
		{
			height++;
		}
		return height;
	}
	// Grow the skiplist by 1
	private void growSkipList()
	{
		int growthLevel = head.height - 1;
		head.grow();
		if (head.next(growthLevel) != null)
		{
			Node<AnyType> temp = head;
			if (head.next(growthLevel).maybeGrow())
			{
				head.setNext(growthLevel + 1,head.next(growthLevel));
				temp = head.next(growthLevel+1);
			}
		
			Node<AnyType> curr = head.next(growthLevel);
			
			while (curr.next(growthLevel) != null)
			{
				if (curr.next(growthLevel).maybeGrow())
				{
					if (temp.height >= growthLevel+2)
						temp.setNext(growthLevel+1,curr.next(growthLevel));
					temp = curr.next(growthLevel);
				}
				curr = curr.next(growthLevel);
			}
		}
	}
	// Trim the skiplist by 1
	private void trimSkipList()
	{
		int newHeight = head.height - 1;
		Node<AnyType> curr = head;
		Node<AnyType> temp = head;
		while (curr.next(newHeight) != null)
		{
			curr = curr.next(newHeight);
			temp.trim(newHeight);
			temp = curr;
		}
		curr.trim(newHeight);
	}
	// Return the log base 2 of the passed in value if it is not 0
	public static int getMaxHeight(int n)
	{
		if (n == 0)
			return 0;
		double result = Math.log(n)/Math.log(2);
		result = Math.ceil(result);
		return (int)result;
	}

	public static double difficultyRating()
	{
		return 4.5;
	}
	public static double hoursSpent()
	{
		return 12.3;
	}
}
