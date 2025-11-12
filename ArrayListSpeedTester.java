import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ArrayListSpeedTester{

    private static final int[] SIZES = {1000};
    
    private static volatile int sinkInt; 
    private static volatile boolean sinkBool;
    private static volatile Object sinkObj;

    public static void main(String[] args){
        System.out.println("### ArrayList 速度比較ベンチマーク ###");

        for(int N : SIZES){
            if(N == 0) continue; 

            System.out.println("\n==============================================");
            System.out.printf("  テスト開始 (N = %d 要素)\n", N);
            System.out.println("==============================================");

            // T1: add(E e)
            long myAddEnd = testAddAtEnd(true, N);
            long stdAddEnd = testAddAtEnd(false, N);
            printResult("T1: add(E) (末尾)", myAddEnd, stdAddEnd);

            // T2: add(int index, E e)
            long myAddStart = testAddAtStart(true, N);
            long stdAddStart = testAddAtStart(false, N);
            printResult("T2: add(int, E) (先頭)", myAddStart, stdAddStart);

            // T3: addAll(Collection c)
            long myAddAllEnd = testAddAllEnd(true, N);
            long stdAddAllEnd = testAddAllEnd(false, N);
            printResult("T3: addAll(Collection) (末尾)", myAddAllEnd, stdAddAllEnd);

            // T4: addAll(int index, Collection c)
            long myAddAllStart = testAddAllStart(true, N);
            long stdAddAllStart = testAddAllStart(false, N);
            printResult("T4: addAll(int, Collection) (先頭)", myAddAllStart, stdAddAllStart);
            
            // T5: clear()
            long myClear = testClear(true, N);
            long stdClear = testClear(false, N);
            printResult("T5: clear()", myClear, stdClear);

            // T6: clone()
            long myClone = testClone(true, N);
            long stdClone = testClone(false, N);
            printResult("T6: clone()", myClone, stdClone);

            // T7: contains(Object o)
            long myContains = testContains(true, N);
            long stdContains = testContains(false, N);
            printResult("T7: contains() (最悪ケース)", myContains, stdContains);

            // T8: get(int index)
            long myGetRandom = testGetRandom(true, N);
            long stdGetRandom = testGetRandom(false, N);
            printResult("T8: get() (ランダム)", myGetRandom, stdGetRandom);

            // T9: indexOf(Object o)
            long myIndexOf = testIndexOf(true, N);
            long stdIndexOf = testIndexOf(false, N);
            printResult("T9: indexOf() (最悪ケース)", myIndexOf, stdIndexOf);

            // T10: isEmpty()
            long myIsEmpty = testIsEmpty(true, N);
            long stdIsEmpty = testIsEmpty(false, N);
            printResult("T10: isEmpty() & size()", myIsEmpty, stdIsEmpty);

            // T11: lastIndexOf(Object o)
            long myLastIndexOf = testLastIndexOf(true, N);
            long stdLastIndexOf = testLastIndexOf(false, N);
            printResult("T11: lastIndexOf() (最悪ケース)", myLastIndexOf, stdLastIndexOf);

            // T12: remove(int index) (at Start)
            long myRemoveStart = testRemoveIndexStart(true, N);
            long stdRemoveStart = testRemoveIndexStart(false, N);
            printResult("T12: remove(int) (先頭)", myRemoveStart, stdRemoveStart);

            // T13: remove(int index) (at End)
            long myRemoveEnd = testRemoveIndexEnd(true, N);
            long stdRemoveEnd = testRemoveIndexEnd(false, N);
            printResult("T13: remove(int) (末尾)", myRemoveEnd, stdRemoveEnd);

            // T14: remove(Object o)
            long myRemoveObj = testRemoveObject(true, N);
            long stdRemoveObj = testRemoveObject(false, N);
            printResult("T14: remove(Object) (先頭)", myRemoveObj, stdRemoveObj);

            // T15: removeAll(Collection c) (No-op)
            long myRemoveAllNoOp = testRemoveAllNoOp(true, N);
            long stdRemoveAllNoOp = testRemoveAllNoOp(false, N);
            printResult("T15: removeAll() (変更なし)", myRemoveAllNoOp, stdRemoveAllNoOp);

            // T16: removeAll(Collection c) (All)
            long myRemoveAllClear = testRemoveAllClear(true, N);
            long stdRemoveAllClear = testRemoveAllClear(false, N);
            printResult("T16: removeAll() (全削除)", myRemoveAllClear, stdRemoveAllClear);

            // T17: removeIf(Predicate f) (No-op)
            long myRemoveIfNoOp = testRemoveIfNoOp(true, N);
            long stdRemoveIfNoOp = testRemoveIfNoOp(false, N);
            printResult("T17: removeIf() (変更なし)", myRemoveIfNoOp, stdRemoveIfNoOp);

            // T18: removeIf(Predicate f) (All)
            long myRemoveIfClear = testRemoveIfClear(true, N);
            long stdRemoveIfClear = testRemoveIfClear(false, N);
            printResult("T18: removeIf() (全削除)", myRemoveIfClear, stdRemoveIfClear);

            // T19: replaceAll(UnaryOperator o)
            long myReplaceAll = testReplaceAll(true, N);
            long stdReplaceAll = testReplaceAll(false, N);
            printResult("T19: replaceAll()", myReplaceAll, stdReplaceAll);

            // T20: retainAll(Collection c) (No-op)
            long myRetainAllNoOp = testRetainAllNoOp(true, N);
            long stdRetainAllNoOp = testRetainAllNoOp(false, N);
            printResult("T20: retainAll() (変更なし)", myRetainAllNoOp, stdRetainAllNoOp);

            // T21: retainAll(Collection c) (All)
            long myRetainAllClear = testRetainAllClear(true, N);
            long stdRetainAllClear = testRetainAllClear(false, N);
            printResult("T21: retainAll() (全削除)", myRetainAllClear, stdRetainAllClear);

            // T22: set(int index, E e)
            long mySetRandom = testSetRandom(true, N);
            long stdSetRandom = testSetRandom(false, N);
            printResult("T22: set() (ランダム)", mySetRandom, stdSetRandom);

            // T23: sort(Comparator c)
            long mySort = testSort(true, N);
            long stdSort = testSort(false, N);
            printResult("T23: sort()", mySort, stdSort);

            // T24: toArray()
            long myToArray = testToArray(true, N);
            long stdToArray = testToArray(false, N);
            printResult("T24: toArray()", myToArray, stdToArray);

            // T25: toArray(T[] a)
            long myToArrayPreSized = testToArrayPreSized(true, N);
            long stdToArrayPreSized = testToArrayPreSized(false, N);
            printResult("T25: toArray(T[] a)", myToArrayPreSized, stdToArrayPreSized);

            // T26: trimToSize()
            long myTrimToSize = testTrimToSize(true, N);
            long stdTrimToSize = testTrimToSize(false, N);
            printResult("T26: trimToSize()", myTrimToSize, stdTrimToSize);
        }
    }

    
    /**
     * テスト対象のListインスタンスを生成
     */
    private static List<Integer> createList(boolean useMyList){
        if(useMyList){
            return new ArrayList<>(); 
        } else{
            return new java.util.ArrayList<>(); 
        }
    }

    /**
     * 結果をフォーマットして表示
     */
    private static void printResult(String testName, long myTime, long stdTime){
        double myTimeMs = myTime / 1_000_000.0;
        double stdTimeMs = stdTime / 1_000_000.0;
        
        System.out.printf("--- %s ---\n", testName);
        System.out.printf("  自作: %.6f ms\n", myTimeMs);
        System.out.printf("  標準: %.6f ms\n", stdTimeMs);
        
        if(stdTimeMs > 0.000001 && myTimeMs > 0.000001){
            double ratio = (myTimeMs / stdTimeMs);
            System.out.printf("  (自作は標準の %.2f 倍の速度)\n", ratio);
        }
    }

    /**
     * T3, T4用: N個の要素を持つ Collection (標準のArrayList) を作成
     */
    private static Collection<Integer> createCollection(int N){
        Collection<Integer> col = new java.util.ArrayList<>(N);
        for(int i = 0; i < N; i++){
            col.add(i);
        }
        return col;
    }

    /**
     * T16, T20用: 0からN-1までの要素を含む Set を作成
     */
    private static Set<Integer> createFullSet(int N){
        Set<Integer> set = new HashSet<>(N);
        for(int i = 0; i < N; i++){
            set.add(i);
        }
        return set;
    }

    // T1: リストの末尾にN回追加
    private static long testAddAtEnd(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            list.add(i);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T2: リストの先頭にN回追加
    private static long testAddAtStart(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            list.add(0, i);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T3: 末尾にN要素をaddAll
    private static long testAddAllEnd(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        Collection<Integer> itemsToAdd = createCollection(N);
        long startTime = System.nanoTime();
        list.addAll(itemsToAdd);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T4: 先頭にN要素をaddAll
    private static long testAddAllStart(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Collection<Integer> itemsToAdd = createCollection(N);

        long startTime = System.nanoTime();
        list.addAll(0, itemsToAdd);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T5: N要素のリストをclear
    private static long testClear(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        long startTime = System.nanoTime();
        list.clear();
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T6: N要素のリストをclone (100回)
    private static long testClone(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        int loops = (N > 1000) ? 10 : 100; 

        try{
            long startTime = System.nanoTime();
            if(useMyList){
                ArrayList<Integer> myList = (ArrayList<Integer>) list; 
                for(int i = 0; i < loops; i++) sinkObj = myList.clone();
            } else{
                java.util.ArrayList<Integer> stdList = (java.util.ArrayList<Integer>) list;
                for(int i = 0; i < loops; i++) sinkObj = stdList.clone();
            }
            long endTime = System.nanoTime();
            return endTime - startTime;
        } catch(Exception e){
            return 0; 
        }
    }

    // T7: N要素のリストでcontains (N回)
    private static long testContains(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Integer target = N - 1; 

        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkBool = list.contains(target);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T8: N要素のリストでget (N回)
    private static long testGetRandom(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Random rand = new Random(123);
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkInt = list.get(rand.nextInt(N));
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T9: N要素のリストでindexOf (N回)
    private static long testIndexOf(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Integer target = N - 1; 

        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkInt = list.indexOf(target);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T10: isEmptyとsizeをN回実行
    private static long testIsEmpty(boolean useMyList, int N){
        List<Integer> listEmpty = createList(useMyList);
        List<Integer> listFull = createList(useMyList);
        listFull.add(1); 
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkBool = listEmpty.isEmpty();
            sinkInt = listEmpty.size();
            sinkBool = listFull.isEmpty();
            sinkInt = listFull.size();
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T11: N要素のリストでlastIndexOf (N回)
    private static long testLastIndexOf(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Integer target = 0; 

        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkInt = list.lastIndexOf(target);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T12: 先頭からN回remove(index)
    private static long testRemoveIndexStart(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            list.remove(0); 
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T13: 末尾からN回remove(index)
    private static long testRemoveIndexEnd(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            list.remove(list.size() - 1); 
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T14: 先頭からN回remove(Object)
    private static long testRemoveObject(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            list.remove(Integer.valueOf(i)); 
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T15: removeAll (0要素削除)
    private static long testRemoveAllNoOp(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Set<Integer> emptySet = Collections.emptySet();

        long startTime = System.nanoTime();
        list.removeAll(emptySet);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T16: removeAll (全要素削除)
    private static long testRemoveAllClear(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Set<Integer> fullSet = createFullSet(N);

        long startTime = System.nanoTime();
        list.removeAll(fullSet);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T17: removeIf (0要素削除)
    private static long testRemoveIfNoOp(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 

        long startTime = System.nanoTime();
        list.removeIf(i -> false); 
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T18: removeIf (全要素削除)
    private static long testRemoveIfClear(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 

        long startTime = System.nanoTime();
        list.removeIf(i -> true); 
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T19: N要素のリストをreplaceAll
    private static long testReplaceAll(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 

        long startTime = System.nanoTime();
        list.replaceAll(i -> i + 1);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T20: retainAll (全要素保持)
    private static long testRetainAllNoOp(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Set<Integer> fullSet = createFullSet(N);

        long startTime = System.nanoTime();
        list.retainAll(fullSet);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T21: retainAll (0要素保持)
    private static long testRetainAllClear(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Set<Integer> emptySet = Collections.emptySet();

        long startTime = System.nanoTime();
        list.retainAll(emptySet);
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T22: N要素のリストでset (N回)
    private static long testSetRandom(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Random rand = new Random(12345);
        
        long startTime = System.nanoTime();
        for(int i = 0; i < N; i++){
            sinkInt = list.set(rand.nextInt(N), i);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T23: N要素のリストをsort
    private static long testSort(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        
        List<Integer> shuffled = new java.util.ArrayList<>(N);
        for(int i = 0; i < N; i++) shuffled.add(i);
        Collections.shuffle(shuffled, new Random(123));
        list.addAll(shuffled);

        long startTime = System.nanoTime();
        list.sort(Comparator.naturalOrder());
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T24: N要素のリストでtoArray() (100回)
    private static long testToArray(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        int loops = (N > 1000) ? 10 : 100; 

        long startTime = System.nanoTime();
        for(int i = 0; i < loops; i++){
            sinkObj = list.toArray();
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T25: N要素のリストでtoArray(T[] a) (100回)
    private static long testToArrayPreSized(boolean useMyList, int N){
        List<Integer> list = createList(useMyList);
        for(int i = 0; i < N; i++) list.add(i); 
        Integer[] a = new Integer[N];
        int loops = (N > 1000) ? 10 : 100; 

        long startTime = System.nanoTime();
        for(int i = 0; i < loops; i++){
            sinkObj = list.toArray(a);
        }
        long endTime = System.nanoTime();
        return endTime - startTime;
    }

    // T26: trimToSize()
    private static long testTrimToSize(boolean useMyList, int N){
        
        try{
            long startTime = System.nanoTime();
            if(useMyList){
                ArrayList<Integer> myList = new ArrayList<>(N * 2); 
                for(int i = 0; i < N; i++) myList.add(i);
                myList.trimToSize();
            } else{
                java.util.ArrayList<Integer> stdList = new java.util.ArrayList<>(N * 2);
                for(int i = 0; i < N; i++) stdList.add(i);
                stdList.trimToSize();
            }
            long endTime = System.nanoTime();
            return endTime - startTime;
        } catch(Exception e){
            return 0;
        }
    }
}