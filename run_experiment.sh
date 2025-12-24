# !/bin/bash

# --- 設定項目 ---
JAVA_HOME="/home/takalab/PM/openjdk16u-nvm/build/linux-x86_64-server-release/images/jdk"
JAVAC="$JAVA_HOME/bin/javac"
JAVA="$JAVA_HOME/bin/java"
JAVA_OPTS="-Xint -XX:+UseSerialGC -XX:-UseCompressedOops -XX:-UseCompressedClassPointers"
CLASS_NAME="test_replaceAll"
LOG_FILE="experiment_results.csv"
SUMMARY_FILE="experiment_summary.txt"
ITERATIONS=10000  # 繰り返し回数を10000回に設定

# 統計用変数
SUCCESS_COUNT=0
FAILURE_COUNT=0

# CSVのヘッダー作成
if [ ! -f "$LOG_FILE" ]; then
    echo "Iteration,Timestamp,Status,Error_Index,Detail" > "$LOG_FILE"
fi

echo "実験開始: 全 $ITERATIONS 回"
echo "----------------------------------------"

for ((i=1; i<=ITERATIONS; i++))
do
    echo "--- 実験回数: $i / $ITERATIONS ---"

    # 0.NVMデータ初期化
    dd if=/dev/zero of=/mnt/nova_disk/data bs=200M count=1 > /dev/null 2>&1

    # 1.コンパイル
    $JAVAC $CLASS_NAME.java > /dev/null 2>&1

    # 2. 1回目：データ作成 & 実行中Kill
    > output_step1.log
    $JAVA $JAVA_OPTS $CLASS_NAME > output_step1.log 2>&1 &
    JAVA_PID=$!

    # 「始め!!!!!!」が出るのを待機
    while ! grep -q "始め!!!!!!" output_step1.log; do
        if ! kill -0 $JAVA_PID 2>/dev/null; then break; fi
        sleep 0.01
    done

    # 0.1秒後にKill
    sleep 0.3
    kill -9 $JAVA_PID 2>/dev/null
    echo "-> 0.1秒経過：Kill完了"

    # 3. 2回目：復元 & 整合性チェック
    RESULT_OUT=$($JAVA $JAVA_OPTS $CLASS_NAME 2>&1)
    TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")
    
    # 判定
    if echo "$RESULT_OUT" | grep -q "整合性が取れています．"; then
        STATUS="SUCCESS"
        ERROR_IDX="N/A"
        DETAIL="Consistency OK"
        ((SUCCESS_COUNT++))
    else
        STATUS="FAILURE"
        ((FAILURE_COUNT++))
        # エラー詳細の抽出
        if echo "$RESULT_OUT" | grep -q "NullPointerException"; then
            ERROR_IDX="NVM_CRASH"
            DETAIL="NPE: Internal structure broken"
        else
            ERROR_IDX=$(echo "$RESULT_OUT" | grep "要素番号" | head -n 1 | sed -E 's/.*要素番号([0-9]+).*/\1/')
            DETAIL=$(echo "$RESULT_OUT" | grep "要素番号" | head -n 1 | sed 's/,/ /g' | tr -d '\n\r')
        fi
    fi

    # CSVに保存
    echo "$i,$TIMESTAMP,$STATUS,$ERROR_IDX,$DETAIL" >> "$LOG_FILE"
    echo "-> 今回の結果: $STATUS (累計失敗: $FAILURE_COUNT)"
    
    # logファイルの削除
    rm *.log
done

# 実験結果の集計を保存
echo "----------------------------------------"
{
    echo "実験完了日: $(date)"
    echo "総試行回数: $ITERATIONS"
    echo "成功回数 (SUCCESS): $SUCCESS_COUNT"
    echo "失敗回数 (FAILURE): $FAILURE_COUNT"
    echo "失敗率: $(awk "BEGIN {print ($FAILURE_COUNT/$ITERATIONS)*100}")%"
} | tee $SUMMARY_FILE

echo "----------------------------------------"
echo "結果詳細: $LOG_FILE"
echo "集計結果: $SUMMARY_FILE"