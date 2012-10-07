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

package net.mikaboshi.intra_mart.tools.log_stats.formatter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.mikaboshi.intra_mart.tools.log_stats.parser.ParserParameter;
import net.mikaboshi.intra_mart.tools.log_stats.report.PageTimeStat;
import net.mikaboshi.intra_mart.tools.log_stats.report.Report;
import net.mikaboshi.intra_mart.tools.log_stats.report.ReportParameter;
import net.mikaboshi.intra_mart.tools.log_stats.report.TimeSpanStatistics;
import net.mikaboshi.intra_mart.tools.log_stats.report.GrossStatistics.RequestEntry;
import net.mikaboshi.intra_mart.tools.log_stats.report.Report.ExceptionReportEntry;
import net.mikaboshi.intra_mart.tools.log_stats.report.Report.RequestUrlReportEntry;
import net.mikaboshi.intra_mart.tools.log_stats.report.Report.SessionReportEntry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * FreeMarkerのテンプレートを使用して、レポートを生成する。
 *
 * @version 1.0.8
 * @author <a href="https://github.com/cwan">cwan</a>
 */
public class TemplateFileReportFormatter extends AbstractFileReportFormatter {

	private static final Log logger = LogFactory.getLog(TemplateFileReportFormatter.class);

	/** デフォルトテンプレートファイル（クラスパス） */
	private static final String TEMPLATE_FILE_PATH = "HtmlFileReportTemplate.html";

	/**  デフォルトテンプレートファイルの文字コード */
	private static final String TEMPLATE_FILE_CHARSET = "Windows-31J";

	/** カスタムテンプレートファイル */
	private File templateFile = null;

	/** カスタムテンプレートファイルの文字コード */
	private String templateFileCharset = Charset.defaultCharset().toString();

	/**
	 * コンストラクタ
	 * @param output レポートの出力先
	 * @param charset レポートの文字コード
	 * @param parserParameter パーサパラメータ
	 * @param reportParameter レポートパラメータ
	 */
	public TemplateFileReportFormatter(
			File output,
			String charset,
			ParserParameter parserParameter,
			ReportParameter reportParameter) {

		super(output, charset, parserParameter, reportParameter);
	}

	/**
	 * レポートの文字コードはシステムデフォルトを使用するコンストラクタ。
	 * @param output レポートの出力先
	 * @param parserParameter パーサパラメータ
	 * @param reportParameter レポートパラメータ
	 */
	public TemplateFileReportFormatter(
				File output,
				ParserParameter parserParameter,
				ReportParameter reportParameter) {

		super(output, parserParameter, reportParameter);
	}

	/**
	 * カスタムテンプレートファイルを設定する。
	 * @param templateFile
	 * @throws FileNotFoundException
	 */
	public void setTemplateFile(File templateFile) throws FileNotFoundException {

		if (!templateFile.exists() || !templateFile.isFile()) {
			throw new FileNotFoundException(templateFile.getPath());
		}

		this.templateFile = templateFile;
	}

	/**
	 * カスタムテンプレートファイルの文字コードを設定する。
	 * @param templateFileCharset
	 */
	public void setTemplateFileCharset(String templateFileCharset) {
		this.templateFileCharset = templateFileCharset;
	}


	@Override
	public void doFormat(Report report) throws IOException {

		Configuration fmConfig = new Configuration();

		InputStream in = null;
		Reader reader = null;

		try {

			if (this.templateFile != null) {

				// カスタムテンプレート
				in = new BufferedInputStream(new FileInputStream(this.templateFile));
				reader = new InputStreamReader(in, this.templateFileCharset);

			} else {
				// デフォルトテンプレート
				in = new BufferedInputStream(getClass().getResourceAsStream(TEMPLATE_FILE_PATH));

				reader = new InputStreamReader(in, TEMPLATE_FILE_CHARSET);
			}


			Template template = new Template("im-memory", reader, fmConfig);

			Map<String, Object> rootMap = new HashMap<String, Object>();

			rootMap.put("charset", this.charset);
			rootMap.put("reportName", report.getParameter().getName());
			rootMap.put("signature", report.getParameter().getSignature());
			rootMap.put("generatedTime", new Date());

			setParameters(rootMap);
			setTimeSpanStatistics(report, rootMap);
			setRequestPageTimeRank(report, rootMap);
			setRequestUrlRank(report, rootMap);
			setSessionRank(report, rootMap);
			setExceptionRank(report, rootMap);
			setLogFiles(report, rootMap);

			template.process(rootMap, this.writer);

		} catch (TemplateException e) {
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * パラメータを設定する。
	 * @param rootMap
	 */
	private void setParameters(Map<String, Object> rootMap) {

		rootMap.put("parserParameter", this.parserParameter);
		rootMap.put("reportParameter", this.reportParameter);
	}

	/**
	 * 期間別統計を設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setTimeSpanStatistics(Report report, Map<String, Object> rootMap) {

		report.countActiveSessions();

		Map<String, Object> timeSpanStat = new HashMap<String, Object>();
		rootMap.put("timeSpanStat", timeSpanStat);

		timeSpanStat.put("span", report.getParameter().getSpan());

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		timeSpanStat.put("list", list);

		long spanMills = report.getParameter().getSpan() * 60 * 1000;

		for (TimeSpanStatistics stat : report.getTimeSpanStatisticsList()) {

			Map<String, Object> row = new HashMap<String, Object>();
			list.add(row);

			List<Long> pageTimes = stat.getRequestPageTimes();

			PageTimeStat pageTimeStat = new PageTimeStat();
			report.setPageTimeStat(pageTimeStat, pageTimes);

			row.put("startDate", stat.getStartDate());
			row.put("endDate", new Date(stat.getStartDate().getTime() + spanMills));
			row.put("requestCount", stat.getRequestCount());
			row.put("pageTimeSum", pageTimeStat.pageTimeSum);
			row.put("pageTimeAverage", pageTimeStat.pageTimeAverage);
			row.put("pageTimeMin", pageTimeStat.pageTimeMin);
			row.put("pageTimeMedian", pageTimeStat.pageTimeMedian);
			row.put("pageTime90Percent", pageTimeStat.pageTime90Percent);
			row.put("pageTimeMax", pageTimeStat.pageTimeMax);
			row.put("pageTimeStandardDeviation", pageTimeStat.pageTimeStandardDeviation);
			row.put("uniqueUserCount", stat.getUniqueUserCount());
			row.put("uniqueSessionCount", stat.getUniqueSessionCount());
			row.put("activeSessionCount", stat.getActiveSessionCount());
			row.put("exceptionCount", stat.getExceptionCount());
			row.put("transitionCount", stat.getTransitionCount());
			row.put("transitionExceptionCount", stat.getTransitionExceptionCount());
		}
	}

	/**
	 * リクエスト処理時間のランキング（総合トップN）を設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setRequestPageTimeRank(Report report, Map<String, Object> rootMap) {

		Map<String, Object> requestPageTimeRank = new HashMap<String, Object>();
		rootMap.put("requestPageTimeRank", requestPageTimeRank);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		requestPageTimeRank.put("list", list);

		requestPageTimeRank.put("size", report.getParameter().getRequestPageTimeRankSize());
		requestPageTimeRank.put("requestCount", report.getRequestCount());

		for (RequestEntry req : report.getRequestPageTimeRank()) {

			if (req == null) {
				continue;
			}

			Map<String, Object> row = new HashMap<String, Object>();
			list.add(row);

			row.put("requestUrl", req.url);
			row.put("requestPageTime", req.time);
			row.put("date", req.date);
			row.put("sessionId", req.sessionId);
			row.put("userId", report.getUserIdFromSessionId(req.sessionId, ""));
		}
	}

	/**
	 * リクエスト処理時間のランキング（URL別・処理時間合計トップN）を設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setRequestUrlRank(Report report, Map<String, Object> rootMap) {

		Map<String, Object> requestUrlRank = new HashMap<String, Object>();
		rootMap.put("requestUrlRank", requestUrlRank);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		requestUrlRank.put("list", list);

		List<RequestUrlReportEntry> reportList = report.getRequestUrlReportList();

		requestUrlRank.put("size", report.getParameter().getRequestUrlRankSize());
		requestUrlRank.put("total", reportList.size());

		MathContext percentMathContext = new MathContext(2, RoundingMode.HALF_UP);

		int i = 0;

		for (RequestUrlReportEntry entry : reportList) {

			if (++i > report.getParameter().getRequestUrlRankSize()) {
				break;
			}

			Map<String, Object> row = new HashMap<String, Object>();
			list.add(row);

			row.put("url", entry.url);
			row.put("count", entry.count);
			row.put("pageTimeSum", entry.pageTimeSum);
			row.put("pageTimeAverage", entry.pageTimeAverage);
			row.put("pageTimeMin", entry.pageTimeMin);
			row.put("pageTimeMedian", entry.pageTimeMedian);
			row.put("pageTime90Percent", entry.pageTime90Percent);
			row.put("pageTimeMax", entry.pageTimeMax);
			row.put("pageTimeStandardDeviation", entry.pageTimeStandardDeviation);
			row.put("countRate", new BigDecimal(entry.countRate * 100, percentMathContext).doubleValue());
			row.put("pageTimeRate", new BigDecimal(entry.pageTimeRate * 100, percentMathContext).doubleValue());
		}
	}

	/**
	 * リクエスト処理時間のランキング（セッション別・処理時間合計トップN）を設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setSessionRank(Report report, Map<String, Object> rootMap) {

		Map<String, Object> sessionRank = new HashMap<String, Object>();
		rootMap.put("sessionRank", sessionRank);

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		sessionRank.put("list", list);

		List<SessionReportEntry> reportList = report.getSessionReportList();

		sessionRank.put("size", report.getParameter().getSessionRankSize());
		sessionRank.put("total", reportList.size());

		MathContext percentMathContext = new MathContext(2, RoundingMode.HALF_UP);

		int i = 0;

		for (SessionReportEntry entry : reportList) {

			if (++i > report.getParameter().getSessionRankSize()) {
				break;
			}

			Map<String, Object> row = new HashMap<String, Object>();
			list.add(row);

			row.put("sessionId", entry.sessionId);
			row.put("userId", report.getUserIdFromSessionId(entry.sessionId, ""));
			row.put("count", entry.count);
			row.put("pageTimeSum", entry.pageTimeSum);
			row.put("pageTimeAverage", entry.pageTimeAverage);
			row.put("pageTimeMin", entry.pageTimeMin);
			row.put("pageTimeMedian", entry.pageTimeMedian);
			row.put("pageTime90Percent", entry.pageTime90Percent);
			row.put("pageTimeMax", entry.pageTimeMax);
			row.put("pageTimeStandardDeviation", entry.pageTimeStandardDeviation);
			row.put("countRate", new BigDecimal(entry.countRate * 100, percentMathContext).doubleValue());
			row.put("pageTimeRate", new BigDecimal(entry.pageTimeRate * 100, percentMathContext).doubleValue());
			row.put("firstAccessTime", entry.firstAccessTime);
			row.put("lastAccessTime", entry.lastAccessTime);
		}
	}

	/**
	 * 例外回数を設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setExceptionRank(Report report, Map<String, Object> rootMap) {

		List<Map<String, Object>> exceptionList = new ArrayList<Map<String, Object>>();
		rootMap.put("exceptionList", exceptionList);

		for (ExceptionReportEntry e : report.getExceptionReport()) {

			Map<String, Object> row = new HashMap<String, Object>();
			exceptionList.add(row);

			row.put("level", e.level);
			row.put("message", e.message);
			row.put("firstLineOfStackTrace", e.firstLineOfStackTrace);
			row.put("count", e.count);
		}
	}

	/**
	 * 解析対象ログファイルを設定する。
	 * @param report
	 * @param rootMap
	 */
	private void setLogFiles(Report report, Map<String, Object> rootMap) {

		Map<String, Object> logFiles = new HashMap<String, Object>();
		rootMap.put("logFiles", logFiles);

		logFiles.put("requestLogFiles",
				report.getRequestLogFiles() != null ?
				report.getRequestLogFiles() :
				ListUtils.EMPTY_LIST);

		logFiles.put("transitionLogFiles",
				report.getTransitionLogFiles() != null ?
				report.getTransitionLogFiles() :
				ListUtils.EMPTY_LIST);

		logFiles.put("exceptionLogFiles",
				report.getExceptionLogFiles() != null ?
				report.getExceptionLogFiles() :
				ListUtils.EMPTY_LIST);

		logFiles.put("transitionLogOnly",
				CollectionUtils.isEmpty(report.getRequestLogFiles()));
	}
}
