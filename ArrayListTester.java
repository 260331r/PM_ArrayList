import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Spliterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

public class ArrayListTester{

    public static void main(String[] args){
        System.out.println("==============================================");
        System.out.println("1. 自作 ArrayList のテスト開始");
        System.out.println("==============================================");
        runAllTests(true);

        System.out.println("\n\n");
        System.out.println("==============================================");
        System.out.println("2. 標準 java.util.ArrayList のテスト開始");
        System.out.println("==============================================");
        runAllTests(false);
    }

    private static void runAllTests(boolean useMyList){
        testBasicOps(useMyList);
        testCollectionOps(useMyList);
        testIterator(useMyList);
        testListIterator(useMyList);
        testSubList(useMyList);
        testBulkOps(useMyList);
        testFailFast(useMyList);
        testSpliterator(useMyList);
        testExceptions(useMyList);
        testSerialization(useMyList);
        
        testConstructors(useMyList);
        testToArraySpecific(useMyList);
        testNullElements(useMyList);
        testEqualsAndHashCode(useMyList);
    }

    private static List<String> createList(boolean useMyList){
        if(useMyList){
            return new ArrayList<>(); 
        }else{
            return new java.util.ArrayList<>();
        }
    }

    private static void printHeader(String title){
        System.out.println("\n--- "+title+" ---");
    }

    private static void printState(String op,List<?> list){
        String content;
        if(list.size()>10){
            content="[...too long to display...]";
        }else{
            content=Arrays.toString(list.toArray());
        }
        
        System.out.printf("[%s] Size: %d, IsEmpty: %b, Content: %s\n",
                op,list.size(),list.isEmpty(),content);
    }

    private static void testBasicOps(boolean useMyList){
        printHeader("T1: 基本操作 (add, get, set, remove, indexOf)");
        List<String> list=createList(useMyList);
        printState("Initial",list);
        list.add("Apple");
        list.add("Banana");
        list.add(0,"Orange");
        printState("After adds",list);
        System.out.println("[get(1)] Result: "+list.get(1));
        String old=list.set(1,"Grape");
        System.out.println("[set(1)] Old: "+old);
        printState("After set",list);
        System.out.println("[indexOf(\"Banana\")] Result: "+list.indexOf("Banana"));
        System.out.println("[lastIndexOf(\"Grape\")] Result: "+list.lastIndexOf("Grape"));
        String removed=list.remove(0);
        System.out.println("[remove(0)] Removed: "+removed);
        printState("After remove(0)",list);
        boolean removedObj=list.remove("Banana");
        System.out.println("[remove(Obj)] Removed: "+removedObj);
        printState("After remove(Obj)",list);
        list.clear();
        printState("After clear",list);
    }

    private static void testCollectionOps(boolean useMyList){
        printHeader("T2: コレクション操作 (addAll, removeAll, retainAll)");
        List<String> list=createList(useMyList);
        list.addAll(Arrays.asList("A","B","C"));
        printState("Initial addAll",list);
        list.addAll(1,Arrays.asList("X","Y"));
        printState("addAll(1, ...)",list);
        System.out.println("[contains(\"Y\")] Result: "+list.contains("Y"));
        list.removeAll(Arrays.asList("A","C","Z"));
        printState("removeAll",list);
        list.retainAll(Arrays.asList("X","B","W"));
        printState("retainAll",list);
    }

    private static void testIterator(boolean useMyList){
        printHeader("T3: Iterator (next, remove, forEachRemaining)");
        List<String> list=createList(useMyList);
        list.add("One"); list.add("Two"); list.add("Three");
        Iterator<String> it=list.iterator();
        while(it.hasNext()){
            String val=it.next();
            if(val.equals("Two")){
                it.remove();
            }
        }
        printState("Iterator remove 'Two'",list);
        list.add("Four");
        it=list.iterator();
        it.next();
        it.forEachRemaining(s -> System.out.println("[forEachRemaining] "+s));
        printState("After forEachRemaining",list);
    }

    private static void testListIterator(boolean useMyList){
        printHeader("T4: ListIterator (previous, add, set)");
        List<String> list=createList(useMyList);
        list.add("A"); list.add("B"); list.add("C");
        ListIterator<String> lit=list.listIterator(1);
        System.out.println("[lit.next()] "+lit.next());
        lit.add("B.5");
        printState("ListIterator add",list);
        System.out.println("[lit.previous()] "+lit.previous());
        lit.set("B.changed");
        printState("ListIterator set",list);
        lit.remove();
        printState("ListIterator remove",list);
    }

    private static void testSubList(boolean useMyList){
        printHeader("T5: SubList (親リストへの変更反映)");
        List<String> list=createList(useMyList);
        list.addAll(Arrays.asList("0","1","2","3","4","5"));
        printState("Initial",list);
        List<String> sub=list.subList(1,4);
        printState("SubList content",sub);
        sub.set(1,"Two");
        printState("Parent after sub.set",list);
        sub.add(1,"One.5");
        printState("Parent after sub.add",list);
        sub.remove(0);
        printState("Parent after sub.remove",list);
        sub.clear();
        printState("Parent after sub.clear",list);
    }

    private static void testBulkOps(boolean useMyList){
        printHeader("T6: 一括操作 (replaceAll, sort, removeIf)");
        List<String> list=createList(useMyList);
        list.addAll(Arrays.asList("D","A","C","B"));
        list.replaceAll(s -> s.toLowerCase());
        printState("replaceAll",list);
        list.sort(Comparator.naturalOrder());
        printState("sort",list);
        list.removeIf(s -> s.equals("c"));
        printState("removeIf",list);

        list.addAll(Arrays.asList("Z","Y","X"));
        printState("Before sub-bulk",list);
        List<String> sub=list.subList(3,6);
        sub.sort(Comparator.naturalOrder());
        printState("After sub.sort",list);
    }

    private static void testFailFast(boolean useMyList){
        printHeader("T7: フェイルファスト (ConcurrentModificationException)");
        try{
            List<String> list=createList(useMyList);
            list.add("A"); list.add("B");
            Iterator<String> it=list.iterator();
            list.add("C");
            it.next();
            System.out.println("[FailFast Iterator] FAILED: CME expected");
        }catch(ConcurrentModificationException e){
            System.out.println("[FailFast Iterator] SUCCESS: Caught "+e.getClass().getSimpleName());
        }
        
        try{
            List<String> list=createList(useMyList);
            list.add("A"); list.add("B"); list.add("C");
            List<String> sub=list.subList(0,1);
            list.add("D");
            sub.get(0);
            System.out.println("[FailFast SubList] FAILED: CME expected");
        }catch(ConcurrentModificationException e){
            System.out.println("[FailFast SubList] SUCCESS: Caught "+e.getClass().getSimpleName());
        }

        try{
            List<String> list=createList(useMyList);
            list.add("A"); list.add("B"); list.add("C");
            list.forEach(s ->{
                if(s.equals("B")) list.remove(s);
            });
            System.out.println("[FailFast forEach] FAILED: CME expected");
        }catch(ConcurrentModificationException e){
            System.out.println("[FailFast forEach] SUCCESS: Caught "+e.getClass().getSimpleName());
        }
    }

    private static void testSpliterator(boolean useMyList){
        printHeader("T8: Spliterator (trySplit, estimateSize)");
        List<String> list=createList(useMyList);
        list.addAll(Arrays.asList("a","b","c","d","e","f","g","h"));
        Spliterator<String> s1=list.spliterator();
        System.out.println("[s1.estimateSize()] "+s1.estimateSize());
        Spliterator<String> s2=s1.trySplit();
        System.out.println("[s1.estimateSize() after split] "+s1.estimateSize());
        System.out.println("[s2.estimateSize() after split] "+s2.estimateSize());
        System.out.println("[s1.characteristics()] "+s1.characteristics());
        System.out.println("[s2.tryAdvance()] "+s2.tryAdvance(System.out::println));
        System.out.println("[s2.estimateSize()] "+s2.estimateSize());
    }

    private static void testExceptions(boolean useMyList){
        printHeader("T9: 例外処理 (Bounds, NoSuchElement, IllegalState)");
        List<String> list=createList(useMyList);
        list.add("A");
        try{ list.get(5); }catch(IndexOutOfBoundsException e){ System.out.println("[Exception] Caught get(5)"); }
        try{ list.add(5,"Z"); }catch(IndexOutOfBoundsException e){ System.out.println("[Exception] Caught add(5)"); }
        Iterator<String> it=list.iterator();
        it.next();
        try{ it.next(); }catch(NoSuchElementException e){ System.out.println("[Exception] Caught it.next()"); }
        it=list.iterator();
        try{ it.remove(); }catch(IllegalStateException e){ System.out.println("[Exception] Caught it.remove() before next()"); }
    }

    private static void testSerialization(boolean useMyList){
        printHeader("T10: シリアライズ (writeObject/readObject)");
        List<String> originalList=createList(useMyList);
        originalList.add("Serialize");
        originalList.add("Me");
        originalList.add(null);
        printState("Original",originalList);

        try{
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ObjectOutputStream oos=new ObjectOutputStream(baos);
            oos.writeObject(originalList);
            oos.close();
            byte[] bytes=baos.toByteArray();
            System.out.println("[Serialize] Byte size: "+bytes.length);

            ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
            ObjectInputStream ois=new ObjectInputStream(bais);
            List<String> restoredList=(List<String>)ois.readObject();
            ois.close();

            printState("Restored",restoredList);
            System.out.println("[Serialize] Content equals: "+originalList.equals(restoredList));

        }catch(Exception e){
            System.out.println("[Serialize] FAILED: "+e.toString());
        }
    }
    
private static void testConstructors(boolean useMyList){
        printHeader("T11: コンストラクタ (Collection, initialCapacity)");
        
        List<String> source=new java.util.ArrayList<>(Arrays.asList("One","Two"));
        List<String> listFromCollection;
        try{
            if(useMyList){
                listFromCollection=new ArrayList<String>(source); // <String> を明記
            }else{
                listFromCollection=new java.util.ArrayList<String>(source); // こちらも合わせて修正
            }
            printState("From Collection",listFromCollection);
            System.out.println("[Constructor] Content equals source: "+listFromCollection.equals(source));
        }catch(Exception e){
            System.out.println("[Constructor] FAILED: Collection constructor threw "+e);
        }

        try{
            List<String> listWithCapacity;
            if(useMyList){
                listWithCapacity=new ArrayList<String>(100); // <String> を明記
            }else{
                listWithCapacity=new java.util.ArrayList<String>(100); // こちらも合わせて修正
            }
            printState("With Capacity 100",listWithCapacity);
            
            if(useMyList){
                new ArrayList<String>(-1); // <String> を明記
            }else{
                new java.util.ArrayList<String>(-1); // こちらも合わせて修正
            }
            System.out.println("[Constructor] FAILED: Negative capacity should throw exception");
        }catch(IllegalArgumentException e){
            System.out.println("[Constructor] SUCCESS: Caught negative capacity: "+e.getClass().getSimpleName());
        }catch(Exception e){
            System.out.println("[Constructor] FAILED: Capacity constructor threw unexpected "+e);
        }
    }
    
    private static void testToArraySpecific(boolean useMyList){
        printHeader("T12: toArray(T[] a) のテスト");
        List<String> list=createList(useMyList);
        list.addAll(Arrays.asList("A","B","C"));

        try{
            String[] smallArray=new String[1];
            String[] result1=list.toArray(smallArray);
            System.out.println("[toArray(small)] Array same object: "+(smallArray==result1));
            System.out.println("[toArray(small)] Content: "+Arrays.toString(result1));

            String[] exactArray=new String[3];
            String[] result2=list.toArray(exactArray);
            System.out.println("[toArray(exact)] Array same object: "+(exactArray==result2));
            System.out.println("[toArray(exact)] Content: "+Arrays.toString(result2));

            String[] largeArray=new String[5];
            largeArray[3]="Extra";
            largeArray[4]="Extra2";
            String[] result3=list.toArray(largeArray);
            System.out.println("[toArray(large)] Array same object: "+(largeArray==result3));
            System.out.println("[toArray(large)] Content: "+Arrays.toString(result3));

        }catch(Exception e){
            System.out.println("[toArray(T[])] FAILED: Threw unexpected "+e);
        }
    }

    private static void testNullElements(boolean useMyList){
        printHeader("T13: null 要素のテスト");
        List<String> list=createList(useMyList);
        try{
            list.add("A");
            list.add(null);
            list.add("B");
            printState("add(null)",list);
            System.out.println("[contains(null)] "+list.contains(null));
            System.out.println("[indexOf(null)] "+list.indexOf(null));
            list.remove(null);
            printState("remove(null)",list);
            System.out.println("[contains(null) after remove] "+list.contains(null));
        }catch(Exception e){
            System.out.println("[NullElements] FAILED: Threw unexpected "+e);
        }
    }

    private static void testEqualsAndHashCode(boolean useMyList){
        printHeader("T14: equals / hashCode のテスト");
        List<String> list1=createList(useMyList);
        list1.addAll(Arrays.asList("A","B","C"));
        
        List<String> list2_standard=new java.util.ArrayList<>();
        list2_standard.addAll(Arrays.asList("A","B","C"));

        List<String> list3_order_diff=new java.util.ArrayList<>();
        list3_order_diff.addAll(Arrays.asList("A","C","B"));
        
        List<String> list4_with_null=createList(useMyList);
        list4_with_null.addAll(Arrays.asList("A",null,"C"));
        
        List<String> list5_standard_null=new java.util.ArrayList<>();
        list5_standard_null.addAll(Arrays.asList("A",null,"C"));

        System.out.println("[equals] 自身との比較: "+list1.equals(list1));
        System.out.println("[equals] 標準リストとの比較: "+list1.equals(list2_standard));
        System.out.println("[equals] 順序違いとの比較: "+list1.equals(list3_order_diff));
        System.out.println("[equals] null入りリストと標準null入りの比較: "+list4_with_null.equals(list5_standard_null));
        
        System.out.println("[hashCode] 自身のhashCode: "+list1.hashCode());
        System.out.println("[hashCode] 標準リストのhashCode: "+list2_standard.hashCode());
        System.out.println("[hashCode] 一致確認: "+(list1.hashCode()==list2_standard.hashCode()));
        System.out.println("[hashCode] null入りリストの一致確認: "+(list4_with_null.hashCode()==list5_standard_null.hashCode()));
    }
}