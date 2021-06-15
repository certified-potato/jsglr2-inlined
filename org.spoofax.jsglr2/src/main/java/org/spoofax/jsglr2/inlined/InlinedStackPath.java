package org.spoofax.jsglr2.inlined;

/**
 * Represents a valid path through the stack graph, represented as a linked list.
 */
public abstract class InlinedStackPath {
    
    /**
     * Length of the path.
     */
    final int length;

    InlinedStackPath(int length) {
        this.length = length;
    }
    
    /**
     * @return Whether there are nodes in the path after this one.  
     */
    abstract boolean isEmpty();

    /**
     * @return The stack node held in this path node.
     */
    abstract InlinedStackNode head();

    /**
     * @return look up whether a specific link exists down this path.
     */
    abstract boolean contains(InlinedStackLink link);
    
    /**
     * A path that contains a single stack node and stops there.
     */
    static final class Empty extends InlinedStackPath {

        private final InlinedStackNode stackNode;
        
        Empty(InlinedStackNode stackNode) {
            super(0);
            this.stackNode = stackNode;
        }
        
        @Override boolean isEmpty() {
            return true;
        }

        @Override InlinedStackNode head() {
            return this.stackNode;
        }

        @Override boolean contains(InlinedStackLink link) {
            return false;
        }
    }
    
    /**
     * A path that has multiple nodes in its list, realized as a linked list.
     */
    static final class NonEmpty extends InlinedStackPath {

        final InlinedStackPath tail;
        final InlinedStackLink link;

        NonEmpty(InlinedStackLink stackLink, InlinedStackPath tail) {
            super(tail.length + 1);
            this.tail = tail;
            this.link = stackLink;
        }

        @Override boolean isEmpty() {
            return false;
        }

        @Override InlinedStackNode head() {
            return this.link.to;
        }

        @Override boolean contains(InlinedStackLink link) {
            return this.link == link || (this.tail != null && this.tail.contains(link));
        }
        
    }
}
