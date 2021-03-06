package de.tum.bio.proteomics.io.searchengine.maxquant;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.bio.proteomics.AnalysisSummary;
import de.tum.bio.proteomics.analysis.AnalysisComponent;
import de.tum.bio.proteomics.headers.SummaryTableHeaders;
import de.tum.bio.proteomics.headers.TableHeaders;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MQSummaryReader extends MQTablesIO implements MQReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	public MQSummaryReader() {
		// empty
	}
	
	@Override
	public boolean fileExists(String txtDirectory, String prefix) {
		return Files.exists(getPath(txtDirectory, prefix));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Enum<E> & TableHeaders> List<AnalysisComponent> read(String txtDirectory, Map<E, String> headerMap) throws IOException {
		List<AnalysisComponent> summary = null;
		List<HashMap<String, String>> contentList = null;
		
		if (headerMap == null) {
			headerMap = (Map<E, String>) initStandardHeaders();
		}
		try {
			Path path = getPath(txtDirectory, "");
			SeparatedTextReader textReader = new SeparatedTextReader();
			progressProperty.bind(textReader.getProgressProperty());
			statusProperty.bind(textReader.getStatusProperty());
			contentList = textReader.readFile(path, headerMap, "\t");
			progressProperty.unbind();
			statusProperty.unbind();
			summary = createListOfSummaries(contentList);
		} catch (IOException e) {
			throw e;
		}
		setProgressProperty(0.0);
		setStatusProperty("");
		
		return summary;
	}
	
	private Path getPath(String txtDirectory, String prefix) {
		return Paths.get(txtDirectory + FileSystems.getDefault().getSeparator() + prefix + FILENAME_SUMMARY);
	}
	
	private List<AnalysisComponent> createListOfSummaries(List<HashMap<String, String>> contentList) {
		List<Map<SummaryTableHeaders, String>> propertiesList = new ArrayList<>();
		setStatusProperty("Collecting analysis properties...");
		for (Map<String, String> properties : contentList) {
			propertiesList.add(standardizeProperties(properties));
		}
		AnalysisSummary summary = new AnalysisSummary(propertiesList);
		List<AnalysisComponent> summaryList = new ArrayList<>();
		summaryList.add(summary);
		return summaryList;
	}

	@Override
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progressProperty;
	}

	@Override
	public ReadOnlyStringProperty getStatusProperty() {
		return statusProperty;
	}
	
	private void setProgressProperty(double value) {
		progressProperty.set(value);
	}
	
	private void setStatusProperty(String status) {
		statusProperty.set(status);
	}
	
	private Map<SummaryTableHeaders, String> standardizeProperties(Map<String, String> properties) {
		Map<SummaryTableHeaders, String> standardizedContentList = new HashMap<SummaryTableHeaders, String>();
		for (Entry<String, String> entry : properties.entrySet()) {
			standardizedContentList.put(SummaryHeadersMap.inverse().get(entry.getKey()), (String) entry.getValue());
		}
		return standardizedContentList;
	}
	
	private Map<SummaryTableHeaders, String> initStandardHeaders() {
		return SUMMARY_HEADERS_UNI;
	}

}
