package com.levenshtein.leven.utility;
import org.apache.log4j.Logger;

import com.levenshtein.leven.utility.exception.QueueEmptyException;
import com.levenshtein.leven.utility.exception.QueueFullException;

/**
 * Todo fix the templating
 * 
 * This code cribbed from http://oppansource.com/queue-implementation-in-java-using-circular-array/ and 
 * subsequently modified slightly by me.
 * 
 * @author pcoates
 * @param <E> The type stored by the Queue
 */
public class CircularQueue<E> {

	static Logger log = Logger.getLogger(CircularQueue.class); 
    private E[] circularQueueAr;
    private int maxSize;   //Maximum Size of Circular Queue
   
    private int rear;//elements will be added/queued at rear.
    private int front;   //elements will be removed/dequeued from front      
    private int number; //number of elements currently in Priority Queue
     /**
      * Constructor
      */
    public CircularQueue(int maxSize){
        this.maxSize = maxSize;
        circularQueueAr = (E[])new Object[this.maxSize];
        number=0; //Initially number of elements in Circular Queue are 0.
        front=0;  
        rear=0;    
    }
 
 
 
    /**
     * Adds element in Circular Queue(at rear)
     */
    public void enqueue(E item) throws QueueFullException {
        if(isFull()){
         throw new QueueFullException("Circular Queue is full");
        }else{
         circularQueueAr[rear] = item;
         rear = (rear + 1) % circularQueueAr.length;    
         number++; // increase number of elements in Circular queue
        }
    }
 
 
    /**
     * Removes element from Circular Queue(from front)
     */
    public E dequeue() throws QueueEmptyException {
        E deQueuedElement;
        if(isEmpty()){
         throw new QueueEmptyException("Circular Queue is empty");
        }else{
           deQueuedElement = circularQueueAr[front];
         circularQueueAr[front] = null;
         front = (front + 1) % circularQueueAr.length;
            number--; // Reduce number of elements from Circular queue
        }
       return deQueuedElement;
    }
 
    /**
     * Return true if Circular Queue is full.
     */
    public boolean isFull() {     
        return (number==circularQueueAr.length);    
    }
 
 
    /**
     * Return true if Circular Queue is empty.
     */
    public boolean isEmpty() {
        return (number==0);
    }
 

//	public static long [] createLongs(int minbits, int maxbits, int seed){
//		long [] longs = new long[256];
//		Set<Long> set = new HashSet<Long>();
//		Random rand = new Random(seed);
//		longs = new long[256];
//		for(int i=0; i<256; i++){
//			long x = rand.nextLong();
//			int numBits=CircularQueue.countSetBits(x);
//			while(numBits<minbits || numBits>maxbits){
//				x=rand.nextLong();
//				numBits=CircularQueue.countSetBits(x);
//				if(set.contains(x)){
//					log.error("createLongs() Collision!? This is one in trillions event....");
//					numBits=0;
//				}
//			}
//			set.add(x);
//			longs[i]=x;
//		}
//		return longs;
//	}
}