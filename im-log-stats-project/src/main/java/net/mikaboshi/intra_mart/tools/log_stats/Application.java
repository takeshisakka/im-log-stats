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

package net.mikaboshi.intra_mart.tools.log_stats;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * アプリケーション設定情報。
 *
 * @since 1.0.11
 * @version 1.0.11
 * @author <a href="https://github.com/cwan">cwan</a>
 */
public class Application {

	private static final Application INSTANCE = new Application();

	private final Properties config = new Properties();

	private Application() {

		InputStream inStream = null;

		try {
			inStream = this.getClass().getResourceAsStream("im-log-stats.properties");

			this.config.load(inStream);

		} catch (IOException e) {
			throw new Error(e);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}

	public static Application getInstance() {
		return INSTANCE;
	}

	/**
	 * プロジェクトバージョンを取得する。
	 * @return
	 */
	public String getProjectVersion() {
		return this.config.getProperty("project.version", "");
	}

	/**
	 * プロジェクトURLを取得する。
	 * @return
	 */
	public String getProjectUrl() {
		return this.config.getProperty("project.url", "");
	}
}
