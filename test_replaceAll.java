/*
    テストの手順は以下の通りである．
    1. データの復元
    2. ArrayListの整合性が取れているかの確認
    3. インデックスと同じ値を要素にする
    4. replaceAllを実行する

    1回目の実行で，replaceAllの最中にタスクキルをする．
    2回目の実行の最初に整合性を確かめるため，そこでreplaceAllの結果を知ることができる．
    replaceAllの引数の処理として，1ミリ秒待ってからn = n + 1を実行する
    テスト実行前に，dd if=/dev/zero of=/mnt/nova_disk/data bs=10G count=1を実行している．
*/
import ourpersist.*;
import java.util.ArrayList; // PM_ArrayListを使う場合は，この行をコメントアウトして下さい．

public class test_replaceAll{
    @durableroot private static ArrayList<Integer> list;

    private static int max_search = 10000;

    public static void main(String[] args) throws Exception{
        Recovery.initNvmFile("/mnt/nova_disk/data");

        if(Recovery.hasEnableNvmData()){
            System.out.println("--- 以前のデータを復元しています ---");
            ClassLoader[] clds = {primeTester.class.getClassLoader()};
            Recovery.recovery(clds);
            System.out.println("復元完了");
        }else{
            System.out.println("--- 新規探索のためNVMを初期化します ---");
            Recovery.init();
        }

        if(list == null){
            list = new ArrayList<>();
        }

        findBoundary(list);
        
        // 2回目以降は，メモリ不足でエラーが出ることがある
        System.out.println("--- 要素割り当て中 ---");
        for(int i = 0; i < max_search; i++){
            list.add(i);
        }

        System.out.println("\n--- replaceAllが実行されます ---");
        try{
            System.out.println("そろそろ始まります");
            Thread.sleep(1000); 
            System.out.println("始め!!!!!!");
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        list.replaceAll(n -> {
            try{
                Thread.sleep(1); 
            }catch(InterruptedException e){}
            return n + 1;
        });
        System.out.println("終わり!!!!!!");
        // 終わりと表示されるまでに，タスクをkillして下さい
    }

    private static void findBoundary(ArrayList<Integer> list){
        boolean foundBoundary = false;

        for(int i = 0; i < list.size() - 1; i++){
            int current = list.get(i);
            int next = list.get(i + 1);

            if(next - current != 1){
                System.out.println("--- 異常検出 ---");
                System.out.println("要素番号" + i + "で，値" + current + "が検出されました．");
                System.out.println("要素番号" + (i + 1) + "の値は，" + next + "です．");
                foundBoundary = true;
            }
        }

        if(!foundBoundary){
            System.out.println("整合性が取れています．");
        }
    }
}