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

package net.mikaboshi.intra_mart.tools.log_stats.parser;

import java.io.File;

import net.mikaboshi.intra_mart.tools.log_stats.entity.Log;


/**
 * ログパーサインターフェース
 *
 * @param <T> Logクラス
 *
 * @version 1.0.8
 * @author <a href="https://github.com/cwan">cwan</a>
 */
public interface LogParser<T extends Log> {

	/**
	 * ログ文字列を解析し、Logオブジェクトとして返す
	 * @param string
	 * @return
	 */
	public abstract T parse(String string);

	/**
	 * 現在解析中のログファイルを設定する
	 * @param logFile
	 */
	public abstract void setLogFile(File logFile);
}