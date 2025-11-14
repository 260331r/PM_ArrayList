import rbp.*; // 松本JVMのAPI

public class primeTester{
    public static void main(String args[]){
        @durableroot
        private static ArrayList<Integer> primeList;

        private static int max_search = 10000;

        public static void main(String args[]){
            Recovery.setNvmFile("nvm_storage.dat");

            // データの復元，もしくは初期化
            if(Recovery.hasValidData()){
                System.out.println("--- 以前のデータを復元しています ---");
                ClassLoader[] clds = {primeTester.class.getClassLoader()};
                Recovery.recovery(clds);
                System.out.println("復元完了");
            }else{
                System.out.println("--- 新規探索のためNVMを初期化します ---");
                Recovery.init();
            }

            if(primeList == null){
                primeList = new ArrayList<>();
            }

            int startNum;
            if(primeList.isEmpty()){
                startNum = 2;
                System.out.println("2から探索を始める");
            }else{
                int lastPrime = primeList.get(primeList.size() - 1);
                startNum = lastPrime + 1;
                System.out.println("前回の続き" + startNum + "から探索を始める");
            }

            // この無限ループ処理の途中でkill -9を実行
            for(int i = startNum; i <= max_search; i++){
                if(isPrime(i)){
                    primeList.add(i);
                    System.out.println("素数発見: " + i + "  現在の要素数: " + primeList.size());
                }
            }
        }
    }

    private static boolean isPrime(int num){
        if(num < 2) return false;
        for(int i = 2; (long)i * i <= num; i++){
            if(num % i == 0) return false;
        }
        return true;
    }
}