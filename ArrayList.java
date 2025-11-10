import java.util.List;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.io.Serializable;
import java.lang.Cloneable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.UnaryOperator;
import java.util.function.Predicate;
import java.util.function.Consumer;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Spliterator;


public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable{
    private transient SafeArrayList internal;

    public ArrayList(){
        internal = new SafeArrayList();
    }

    public ArrayList(int initialCapacity){
        internal = new SafeArrayList(initialCapacity);
    }

    public ArrayList(Collection<? extends E> c){
        Objects.requireNonNull(c);
        Object[] a = c.toArray();
        int size = a.length;
        
        if(a.getClass() != Object[].class){
            a = Arrays.copyOf(a, size, Object[].class);
        }
        
        this.internal = new SafeArrayList(a, size, 0);
    }

    // 要素を末尾に追加
    public boolean add(E e){
        Object[] shadowData;

        if(internal.size >= internal.data.length){
            shadowData = internal.resize();
        }else{
            shadowData = internal.copy();
        }
        shadowData[internal.size] = e;

        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size + 1, internal.ModCount + 1);
        internal = shadow;

        return true;
    }

    // 指定位置に要素を追加
    public void add(int index, E element){
        if(index < 0 || index > internal.size){
            throw new IndexOutOfBoundsException();           
        }

        Object[] shadowData;

        if(internal.size >= internal.data.length){
            shadowData = internal.resize();
        }else{
            shadowData = internal.copy();
        }

        System.arraycopy(internal.data, 0, shadowData, 0, index);
        System.arraycopy(internal.data, index, shadowData, index + 1, internal.size - index);
        shadowData[index] = element;
        
        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size + 1, internal.ModCount + 1);
        internal = shadow;
    }

    // 指定されたコレクション内のすべての要素をリストの末尾に追加する
    public boolean addAll(Collection<? extends E> c){
        Object[] addList = c.toArray();
        Object[] shadowData;
        if(internal.data.length > internal.size + addList.length){
            shadowData = new Object[internal.data.length];
        }else{
            shadowData = new Object[Math.max(internal.size + addList.length, internal.data.length * 2 + 1)];
        }

        System.arraycopy(internal.data, 0, shadowData, 0, internal.size);
        System.arraycopy(addList, 0, shadowData, internal.size, addList.length);

        SafeArrayList shadow;
        if(addList.length > 0){
            shadow = new SafeArrayList(shadowData, internal.size + addList.length, internal.ModCount + 1);
        }else{
            shadow = new SafeArrayList(shadowData, internal.size + addList.length, internal.ModCount);
        }
        internal = shadow;

        return (addList.length > 0);
    }

    // 指定されたコレクション内のすべての要素をリストの指定のインデックスの場所に追加する
    public boolean addAll(int index, Collection<? extends E> c){
        if(index < 0 || index > internal.size){
            throw new IndexOutOfBoundsException();
        }

        Object[] addList = c.toArray();
        Object[] shadowData;
        if(internal.data.length > internal.size + addList.length){
            shadowData = new Object[internal.data.length];
        }else{
            shadowData = new Object[Math.max(internal.size + addList.length, internal.data.length * 2 + 1)];
        }

        System.arraycopy(internal.data, 0, shadowData, 0, index);
        System.arraycopy(addList, 0, shadowData, index, addList.length);
        System.arraycopy(internal.data, index, shadowData, index + addList.length, internal.size - index);

        SafeArrayList shadow;
        if(addList.length > 0){
            shadow = new SafeArrayList(shadowData, internal.size + addList.length, internal.ModCount + 1);
        }else{
            shadow = new SafeArrayList(shadowData, internal.size + addList.length, internal.ModCount);
        }
        internal = shadow;

        return (addList.length > 0);
    }

    // すべての要素を削除
    public void clear(){
        int arraySize = internal.data.length;
        Object[] shadowData = new Object[arraySize];
        
        SafeArrayList shadow = new SafeArrayList(shadowData, 0, internal.ModCount + 1);
        internal = shadow;
    }

    // ArrayListインスタンスのコピーを返す
    public Object clone(){
        Object[] shadowData = internal.copy();
        SafeArrayList shadowInternal = new SafeArrayList(shadowData, internal.size, internal.ModCount);
        ArrayList<E> shadowList = new ArrayList<>();
        shadowList.internal = shadowInternal;
        return shadowList;
    }

    // 指定の値が要素に含まれているかを調べる
    public boolean contains(Object o){
        return (indexOf(o) >= 0);
    }

    // 内部配列のサイズを最低minCapacityにする関数
    public void ensureCapacity(int minCapacity){
        if(minCapacity <= internal.data.length){
            return;
        }

        int newCapacity = Math.max(minCapacity, internal.data.length * 2 + 1);
        Object[] shadowData = new Object[newCapacity];
        System.arraycopy(internal.data, 0, shadowData, 0, internal.size);

        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size, internal.ModCount + 1);
        internal = shadow;
    }

    // 指定位置の要素を取得
    @SuppressWarnings("unchecked")
    public E get(int index){
        if(index < 0 || index >= internal.size){
            throw new IndexOutOfBoundsException();
        }

        return (E) internal.data[index];
    }

    // 指定の処理をすべての要素で実行
    public void forEach(Consumer<? super E> action){
        Objects.requireNonNull(action);
        final int expectedModCount = internal.ModCount;
        final int size = internal.size;
        final Object[] data = internal.data;

        for(int i = 0; i < size; i++){
            if(internal.ModCount != expectedModCount){
                throw new ConcurrentModificationException();
            }
            @SuppressWarnings("unchecked")
            E elem = (E) data[i];
            action.accept(elem);
        }

        if(internal.ModCount != expectedModCount){
            throw new ConcurrentModificationException();
        }
    }

    // 指定の値が最初に現れたインデックスを返す
    public int indexOf(Object o){
        for(int i = 0; i < internal.size; i++){
            if(Objects.equals(o, internal.data[i])) return i;
        }

        return -1;
    }

    public ListIterator<E> listIterator(){
        ListIterator<E> li = new MyListIterator();
        return li;
    }

    public ListIterator<E> listIterator(int index){
        ListIterator<E> li = new MyListIterator(index);
        return li;
    }

    // 空かどうか判定
    public boolean isEmpty(){
        return (internal.size == 0);
    }

    // イテレーターを返す
    public Iterator<E> iterator(){
        Iterator<E> it = new MyIterator<E>();
        return it;
    }

    // 指定の値が最後に現れたインデックスを返す
    public int lastIndexOf(Object o){
        for(int i = internal.size - 1; i >= 0; i--){
            if(Objects.equals(o, internal.data[i])) return i;
        }

        return -1;
    }

    // 指定のインデックスの要素を削除し，返す
    @SuppressWarnings("unchecked")
    public E remove(int index){
        if(index < 0 || index >= internal.size){
            throw new IndexOutOfBoundsException();           
        }

        E oldValue = (E) internal.data[index];
        Object[] shadowData = internal.copy();

        System.arraycopy(internal.data, index + 1, shadowData, index, internal.size - index - 1);
        shadowData[internal.size - 1] = null;
        
        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size - 1, internal.ModCount + 1);
        internal = shadow;

        return oldValue;
    }    

    // 指定の要素で，一番最初に現れるものを削除
    public boolean remove(Object o){
        int index = indexOf(o);
        if(index >= 0){
            remove(index);
            return true;
        }
        return false;
    }

    // リストから，指定されたコレクション内に保持されているすべての要素を削除
    public boolean removeAll(Collection<?> c){
        Objects.requireNonNull(c);
        return filterByPredicate(elem -> !c.contains(elem));
    }

    // リストから，引数の条件に合う要素を削除する
    @SuppressWarnings("unchecked")
    public boolean removeIf(Predicate<? super E> filter){
        Objects.requireNonNull(filter);
        return filterByPredicate(elem -> !filter.test((E) elem));
    }

    // 指定インデックスの範囲の要素を削除する
    protected void removeRange(int fromIndex, int toIndex){
        if(fromIndex < 0 || toIndex > internal.size || fromIndex > toIndex){
            throw new IndexOutOfBoundsException();
        }

        Object[] shadowData = new Object[internal.data.length];
        int newSize = internal.size - (toIndex - fromIndex);

        System.arraycopy(internal.data, 0, shadowData, 0, fromIndex);
        System.arraycopy(internal.data, toIndex, shadowData, fromIndex, internal.size - toIndex);

        SafeArrayList shadow;
        if(fromIndex != toIndex){
            shadow = new SafeArrayList(shadowData, newSize, internal.ModCount + 1);
        }else{
            shadow = new SafeArrayList(shadowData, newSize, internal.ModCount);
        }
        internal = shadow;
    }

    // リストの各要素に引数の演算を実行する
    public void replaceAll(UnaryOperator<E> operator){
        Objects.requireNonNull(operator);
        Object[] shadowData = internal.copy();

        for(int i = 0; i < internal.size; i++){
            @SuppressWarnings("unchecked")
            E oldValue = (E) shadowData[i];
            shadowData[i] = operator.apply(oldValue);
        }

        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size, internal.ModCount + 1);
        internal = shadow;
    }

    // リストから，指定されたコレクション内に保持されている要素のみを抽出
    public boolean retainAll(Collection<?> c){
        Objects.requireNonNull(c);
        return filterByPredicate(elem -> c.contains(elem));
    }

    // 指定された位置の要素を，指定された要素に置き換え
    @SuppressWarnings("unchecked")
    public E set(int index, E element){
        if(index < 0 || index >= internal.size){
            throw new IndexOutOfBoundsException();           
        }

        Object[] shadowData = internal.copy();

        E oldValue = (E) shadowData[index];
        shadowData[index] = element;

        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size, internal.ModCount);
        internal = shadow;

        return oldValue;
    }

    // 要素数を取得
    public int size(){
        return internal.size;
    }

    // 引数で指定された順序に則ってリストをソートする
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c){
        Object[] shadowData = internal.copy();
        Arrays.sort(shadowData, 0, internal.size, (Comparator<Object>) c);

        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size, internal.ModCount + 1);
        internal = shadow;
    }

    @Override
    public Spliterator<E> spliterator(){
        Spliterator<E> spl= new MySpliterator(this, 0, -1, 0);
        return spl;
    }

    // サブリストを返す
    public List<E> subList(int fromIndex, int toIndex){
        if(fromIndex < 0 || toIndex > internal.size || fromIndex > toIndex){
            throw new IndexOutOfBoundsException();
        }

        List<E> sub = new MySubList(fromIndex, toIndex);
        return sub;
    }

    // ArrayListの配列を返す
    public Object[] toArray(){
        return shrink();
    }

    // ArrayListの配列を引数の配列にコピーして返す
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a){
        if(a.length < internal.size){
            return (T[]) Arrays.copyOf(internal.data, internal.size, a.getClass());
        }

        System.arraycopy(internal.data, 0, a, 0, internal.size);
        if(a.length > internal.size){
            a[internal.size] = null;
        }

        return a;
    }

    // 配列サイズを要素数に合わせる
    public void trimToSize(){
        if(internal.data.length == internal.size){
            return;
        }
        Object[] shadowData = shrink();
        SafeArrayList shadow = new SafeArrayList(shadowData, internal.size, internal.ModCount + 1);
        internal = shadow;
    }

    // 配列の余分な部分を切り落とす
    private Object[] shrink(){
        Object[] newData = new Object[internal.size];
        System.arraycopy(internal.data, 0, newData, 0, internal.size);

        return newData;
    }

    // 条件で要素を絞る処理
    private boolean filterByPredicate(Predicate<Object> predicate){
        Object[] shadowData = new Object[internal.data.length];
        int newSize = 0;
        boolean modified = false;

        for(int i = 0; i < internal.size; i++){
            Object elem = internal.data[i];
            if(predicate.test(elem)){
                shadowData[newSize++] = elem;
            }else{
                modified = true;
            }
        }

        if(modified){
            SafeArrayList shadow = new SafeArrayList(shadowData, newSize, internal.ModCount + 1);
            internal = shadow;
        }

        return modified;
    }

    // ArrayListの本体
    private class SafeArrayList{
        private final Object[] data;
        private final int size;
        private final int ModCount;

        private SafeArrayList(){
            this.data = new Object[10];
            this.size = 0;
            this.ModCount = 0;
        }

        private SafeArrayList(int initialCapacity){
            if(initialCapacity < 0){
                throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
            }
            this.data = new Object[initialCapacity];
            this.size = 0;
            this.ModCount = 0;
        }

        private SafeArrayList(Object[] data, int size, int ModCount){
            this.data = data;
            this.size = size;
            this.ModCount = ModCount;
        }

        // 現在の配列のコピーを作る
        private Object[] copy(){
            return makeArray(data.length);
        }

        // 配列のサイズを拡張する
        private Object[] resize(){
            return makeArray(size * 2 + 1);
        }

        // 配列を作り直す
        private Object[] makeArray(int newSize){
            Object[] shadowData = new Object[newSize];
            System.arraycopy(data, 0, shadowData, 0, size);

            return shadowData;
        }
    }

    private class MyIterator<E> implements Iterator<E>{
        private int cursor;
        private int expectedModCount;

        private MyIterator(){
            this.cursor = 0;
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        private void checkForComodification(){
            if(ArrayList.this.internal.ModCount != this.expectedModCount){
                throw new ConcurrentModificationException();
            }
        }
        public boolean hasNext(){
            return (this.cursor < internal.size);
        }

        @SuppressWarnings("unchecked")
        public E next(){
            checkForComodification();
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            E elem = (E) internal.data[this.cursor];
            this.cursor++;
            return (E) elem;
        }

        public void remove(){
            checkForComodification();
            if(this.cursor - 1 < 0){
                throw new IllegalStateException();
            }
            this.cursor--;
            ArrayList.this.remove(this.cursor);
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        // forEachRemainingは親クラスのメソッドを利用する
        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action){
            Objects.requireNonNull(action);

            final Object[] data = ArrayList.this.internal.data;
            final int size = ArrayList.this.internal.size;
            int i = this.cursor;

            if(i >= size){
                return;
            }

            final int currentExpectedModCount = ArrayList.this.internal.ModCount;
            for(; ArrayList.this.internal.ModCount == currentExpectedModCount && i < size; i++){
                action.accept((E) data[i]);
            }

            this.cursor = i;
            if(ArrayList.this.internal.ModCount != currentExpectedModCount){
                throw new ConcurrentModificationException();
            }
        }
    }

    private class MyListIterator implements ListIterator<E>{
        private int cursor;
        private int lastRet = -1;
        private int expectedModCount;

        private MyListIterator(){
            this(0);
        }

        private MyListIterator(int index){
            if(index < 0 || index > internal.size){
                throw new IndexOutOfBoundsException();
            }
            this.cursor = index;
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        private void checkForComodification(){
            if(ArrayList.this.internal.ModCount != this.expectedModCount){
                throw new ConcurrentModificationException();
            }
        }

        public boolean hasNext(){
            return (this.cursor < internal.size);
        }

        @SuppressWarnings("unchecked")
        public E next(){
            checkForComodification();
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            E elem = (E) internal.data[cursor];
            this.lastRet = this.cursor;
            this.cursor++;
            return (E) elem;
        }

        public boolean hasPrevious(){
            return (this.cursor > 0);
        }

        public E previous(){
            checkForComodification();
            if(!hasPrevious()){
                throw new NoSuchElementException();
            }
            this.cursor--;
            E elem = ArrayList.this.get(this.cursor);
            this.lastRet = this.cursor;
            return elem;
        }

        public int nextIndex(){
            return this.cursor;
        }

        public int previousIndex(){
            return (this.cursor - 1);
        }

        public void remove(){
            checkForComodification();
            if(this.lastRet < 0){
                throw new IllegalStateException();
            }

            ArrayList.this.remove(this.lastRet);
            this.cursor = this.lastRet;
            this.lastRet = -1;
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        public void add(E e){
            checkForComodification();
            ArrayList.this.add(this.cursor, e);
            this.cursor++;
            this.lastRet = -1;
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        public void set(E e){
            checkForComodification();
            if(this.lastRet < 0){
                throw new IllegalStateException();
            }
            ArrayList.this.set(this.lastRet, e);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action){
            Objects.requireNonNull(action);

            final Object[] data = ArrayList.this.internal.data;
            final int size = ArrayList.this.internal.size;
            int i = this.cursor;

            if(i >= size){
                return;
            }

            final int currentExpectedModCount = ArrayList.this.internal.ModCount;
            for(; ArrayList.this.internal.ModCount == currentExpectedModCount && i < size; i++){
                action.accept((E) data[i]);
            }

            this.cursor = i;
            this.lastRet = -1;
            if(ArrayList.this.internal.ModCount != currentExpectedModCount){
                throw new ConcurrentModificationException();
            }
        }
    }

    private class MySubList extends AbstractList<E>{
        private final int offset;
        private int size;
        private int expectedModCount;

        private MySubList(int fromIndex, int toIndex){
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        private void checkForComodification(){
            if(ArrayList.this.internal.ModCount != this.expectedModCount){
                throw new ConcurrentModificationException();
            }
        }

        private void rangeCheck(int index){
            if(index < 0 || index >= this.size){
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
            }
        }

        private void rangeCheckForAdd(int index){
            if(index < 0 || index > this.size){
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
            }
        }

        @Override
        public int size(){
            checkForComodification();
            return this.size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E get(int index){
            rangeCheck(index);
            checkForComodification();
            return (E) ArrayList.this.internal.data[offset + index];
        }

        @Override
        public E set(int index, E element){
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.set(offset + index, element);
            return oldValue;
        }

        @Override
        public void add(int index, E element){
            rangeCheckForAdd(index);
            checkForComodification();

            ArrayList.this.add(offset + index, element);
            this.expectedModCount = ArrayList.this.internal.ModCount;
            this.size++;
        }

        @Override
        public E remove(int index){
            rangeCheck(index);
            checkForComodification();

            E result = ArrayList.this.remove(offset + index);

            this.expectedModCount = ArrayList.this.internal.ModCount;
            this.size--;
            return result;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex){
            checkForComodification();
            ArrayList.this.removeRange(offset + fromIndex, offset + toIndex);
            this.expectedModCount = ArrayList.this.internal.ModCount;
            this.size -= toIndex - fromIndex;
        }


        @Override
        @SuppressWarnings("unchecked")
        public void replaceAll(UnaryOperator<E> operator){
            Objects.requireNonNull(operator);
            checkForComodification();

            final SafeArrayList parentInternal = ArrayList.this.internal;
            final Object[] shadowData = parentInternal.copy();

            for(int i = offset; i < offset + size; i++){
                shadowData[i] = operator.apply((E) shadowData[i]);
            }

            ArrayList.this.internal = new SafeArrayList(shadowData, parentInternal.size, parentInternal.ModCount + 1);
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void sort(Comparator<? super E> c){
            checkForComodification();

            final SafeArrayList parentInternal = ArrayList.this.internal;
            final Object[] shadowData = parentInternal.copy();

            Arrays.sort(shadowData, offset, offset + size, (Comparator<Object>) c);
            ArrayList.this.internal = new SafeArrayList(shadowData, parentInternal.size, parentInternal.ModCount + 1);
            this.expectedModCount = ArrayList.this.internal.ModCount;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean removeIf(Predicate<? super E> filter){
            Objects.requireNonNull(filter);
            checkForComodification();

            final SafeArrayList parentInternal = ArrayList.this.internal;
            final Object[] data = parentInternal.data;
            final int parentSize = parentInternal.size;

            Object[] shadowData = new Object[parentInternal.data.length];
            System.arraycopy(data, 0, shadowData, 0, offset);

            int writeIndex = offset;
            boolean modified = false;

            for(int i = offset; i < offset + size; i++){
                Object elem = data[i];
                if(filter.test((E) elem)){
                    modified = true;
                }else{
                    shadowData[writeIndex++] = elem;
                }
            }

            if(!modified){
                return false;
            }

            int removedCount = (offset + size) - writeIndex;
            int newParentSize = parentSize - removedCount;

            System.arraycopy(data, offset + size, shadowData, writeIndex, parentSize - (offset + size));
            ArrayList.this.internal = new SafeArrayList(shadowData, newParentSize, parentInternal.ModCount + 1);

            this.expectedModCount = ArrayList.this.internal.ModCount;
            this.size -= removedCount;

            return true;
        }

        @SuppressWarnings("unchecked")
        public void forEach(Consumer<? super E> action){
            Objects.requireNonNull(action);
            checkForComodification();

            final Object[] data = ArrayList.this.internal.data;
            final int size = this.size;
            final int offset = this.offset;

            final int currentExpectedModCount = ArrayList.this.internal.ModCount;

            for(int i = 0; ArrayList.this.internal.ModCount == currentExpectedModCount && i < size; i++){
                E elem = (E) data[offset + i];
                action.accept(elem);
            }

            if(ArrayList.this.internal.ModCount != currentExpectedModCount){
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public Spliterator<E> spliterator(){
            checkForComodification();
            Spliterator<E> spl = new MySpliterator(ArrayList.this, this.offset, this.offset + this.size, this.expectedModCount);
            return spl;
        }
    }

    final class MySpliterator implements Spliterator<E>{
        private final ArrayList<E> list;
        private int index;
        private int fence;
        private int expectedModCount;

        private MySpliterator(ArrayList<E> list, int origin, int fence, int expectedModCount){
            this.list = list;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence(){
            int hi = fence;
            if(hi < 0){
                SafeArrayList lst = list.internal;
                expectedModCount = lst.ModCount;
                hi = fence = lst.size;
            }
            return hi;
        }

        @Override
        public Spliterator<E> trySplit(){
            int hi = getFence();
            int lo = index;
            int mid = (lo + hi) >>> 1;

            if(lo >= mid){
                return null;
            }

            Spliterator<E> spl = new MySpliterator(list, lo, mid, expectedModCount);
            this.index = mid;
            return spl;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean tryAdvance(Consumer<? super E> action){
            Objects.requireNonNull(action);
            int hi = getFence();
            int i = index;
            if(i < hi){
                index = i + 1;
                Object[] data = list.internal.data;
                E e = (E) data[i];

                action.accept(e);

                if(list.internal.ModCount != expectedModCount){
                    throw new ConcurrentModificationException();
                }
                return true;
            }

            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> action){
            Objects.requireNonNull(action);
            int hi;
            int i;

            hi = getFence();
            i = index;
            if(i >= hi){
                return;
            }

            index = hi;

            Object[] data = list.internal.data;

            final int currentExpectedModCount = expectedModCount;
            for(; list.internal.ModCount == currentExpectedModCount && i < hi; i++){
                action.accept((E) data[i]);
            }
            if(list.internal.ModCount != currentExpectedModCount){
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public long estimateSize(){
            return (long) (getFence() - index);
        }

        @Override
        public int characteristics(){
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * @param s
     * @throws IOException
     */

    private void writeObject(ObjectOutputStream s) throws IOException{
        final SafeArrayList currentInternal = this.internal;
        final int size = currentInternal.size;

        s.defaultWriteObject();
        s.writeInt(size);

        final Object[] data = currentInternal.data;
        for(int i = 0; i < size; i++){
            s.writeObject(data[i]);
        }
    }

    /** 
     * @param s
     * @throws IOException
     * @throws ClassNotFoundException
     */ 

    private void readObject(ObjectInputStream s) throws IOException,ClassNotFoundException{
        s.defaultReadObject();

        int size = s.readInt();
        if(size < 0){
            throw new IOException("Illegal size: " + size);
        }

        Object[] data = new Object[size];
        for(int i = 0; i < size; i++){
            data[i] = s.readObject();
        }

        this.internal = new SafeArrayList(data, size, 0);
    }
}