/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package net.mikaboshi.intra_mart.tools.log_stats.util;

import java.util.List;

/**
 *
 * @version 1.0.8
 * @author <a href="https://github.com/cwan">cwan</a>
 */
public final class MathUtil {

	private MathUtil() {}

	/**
	 * Longリストの要素の合計を取得する。
	 * @param longList
	 * @return
	 */
	public static long getSum(List<Long> longList) {

		if (longList == null) {
			return 0L;
		}

		long sum = 0L;

		for (long l : longList) {
			sum += l;
		}

		return sum;
	}

	/**
	 * Longリストの要素の平均を取得する。
	 * @param longList
	 * @param sum 合計
	 * @param devByZero リストの要素が0の場合の戻り値
	 * @return
	 */
	public static long getAverage(List<Long> longList, long sum, long devByZero) {

		if (longList == null || longList.isEmpty()) {
			return devByZero;
		}

		return Math.round((double) sum / (double) longList.size());
	}

	/**
	 * Longリストの要素の標準偏差を取得する。
	 * @param longList
	 * @param average
	 * @param devByZero リストの要素が0の場合の戻り値
	 * @return
	 */
	public static long getStandardDeviation(List<Long> longList, long average, long devByZero) {

		if (longList == null || longList.isEmpty()) {
			return devByZero;
		}

		double sd = 0.0d;

		for (long l: longList) {
			sd += Math.pow(l - average, 2);
		}

		sd = Math.sqrt(sd / (double) longList.size());

		return Math.round(sd);
	}

	/**
	 * 比率を計算する
	 * @param numerator 分子
	 * @param denominator 分母
	 * @param devByZeroValue denominatorが0の場合の戻り値
	 * @return
	 */
	public static double getRate(long numerator, long denominator, double devByZeroValue) {

		if (denominator == 0) {
			return devByZeroValue;
		}

		return (double) numerator / (double) denominator;
	}
}
