package org.spoofax.jsglr2.inlined.components;

public abstract class InlinedStackPath {
    
    public final int length;

    protected InlinedStackPath(int length) {
        this.length = length;
    }
    
    public abstract boolean isEmpty();

    public abstract InlinedStackNode head();

    public abstract boolean contains(InlinedStackLink link);
    
    public static final class Empty extends InlinedStackPath {

        private final InlinedStackNode stackNode;
        
        protected Empty(InlinedStackNode stackNode) {
            super(0);
            this.stackNode = stackNode;
        }
        
        @Override public boolean isEmpty() {
            return true;
        }

        @Override public InlinedStackNode head() {
            return this.stackNode;
        }

        @Override public boolean contains(InlinedStackLink link) {
            return false;
        }
    }
    
    public static final class NonEmpty extends InlinedStackPath {

        public final InlinedStackPath tail;
        public final InlinedStackLink link;

        protected NonEmpty(InlinedStackLink stackLink, InlinedStackPath tail) {
            super(tail.length + 1);
            this.tail = tail;
            this.link = stackLink;
        }

        @Override public boolean isEmpty() {
            return false;
        }

        @Override public InlinedStackNode head() {
            return this.link.to;
        }

        @Override public boolean contains(InlinedStackLink link) {
            return this.link == link || (this.tail != null && this.tail.contains(link));
        }
        
    }
}
