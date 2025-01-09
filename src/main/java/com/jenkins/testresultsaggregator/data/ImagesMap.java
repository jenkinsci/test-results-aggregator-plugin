package com.jenkins.testresultsaggregator.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.localizer.Localizable;

import com.jenkins.testresultsaggregator.reports.HTMLReporter;

import hudson.model.HealthReport;

public class ImagesMap {
	
	private static Map<String, ImageData> images = new HashMap<>();
	private static String imageSize = "style=\"width:30px;height:30px;\"";
	private static String getIconSize = "32x32";
	
	public static enum Images {
		image0,
		image1,
		image2,
		image3,
		image4
	}
	
	public static String getIconImagePath(int score) {
		return hudson.model.HealthReport.min(new HealthReport(score, new Localizable(null, null, null)), null).getIconUrl(getIconSize);
	}
	
	public static String getIconImagePath(String contentId) {
		return hudson.model.HealthReport.min(new HealthReport(getImages().get(contentId).getScore(), new Localizable(null, null, null)), null).getIconUrl(getIconSize);
	}
	
	public static String getImage(String contentId) {
		return getImages().get(contentId).getSourcePath();
	}
	
	public static Map<String, ImageData> getImages() {
		String path = "";
		if (!HTMLReporter.FOLDER.isEmpty()) {
			path = HTMLReporter.FOLDER + File.separator;
		}
		images.put(Images.image0.name(), new ImageData(path + "health-00to19.png", "/icons/health-00to19.png", "health-00to19.png", "cid:image0", "Project health is 20% or less", 0));
		images.put(Images.image1.name(), new ImageData(path + "health-20to39.png", "/icons/health-20to39.png", "health-20to39.png", "cid:image1", "Project health is over 20% and up to 40%", 20));
		images.put(Images.image2.name(), new ImageData(path + "health-40to59.png", "/icons/health-40to59.png", "health-40to59.png", "cid:image2", "Project health is over 40% and up to 60%", 40));
		images.put(Images.image3.name(), new ImageData(path + "health-60to79.png", "/icons/health-60to79.png", "health-60to79.png", "cid:image3", "Project health is over 60% and up to 80%", 60));
		images.put(Images.image4.name(), new ImageData(path + "health-80plus.png", "/icons/health-80plus.png", "health-80plus.png", "cid:image4", "Project health is over 80%", 80));
		return images;
	}
	
	public static String getImage(int score) {
		if (score <= 20) {
			return "<img src=\"" + ImagesMap.getImages().get(Images.image0.name()).getFileName() + "\" alt=\"" + ImagesMap.getImages().get(Images.image0.name()).getAlt() + "\" " + imageSize + ">";
		} else if (score <= 40) {
			return "<img src=\"" + ImagesMap.getImages().get(Images.image1.name()).getFileName() + "\" alt=\"" + ImagesMap.getImages().get(Images.image1.name()).getAlt() + "\" " + imageSize + ">";
		} else if (score <= 60) {
			return "<img src=\"" + ImagesMap.getImages().get(Images.image2.name()).getFileName() + "\" alt=\"" + ImagesMap.getImages().get(Images.image2.name()).getAlt() + "\" " + imageSize + ">";
		} else if (score <= 80) {
			return "<img src=\"" + ImagesMap.getImages().get(Images.image3.name()).getFileName() + "\" alt=\"" + ImagesMap.getImages().get(Images.image3.name()).getAlt() + "\" " + imageSize + ">";
		} else {
			return "<img src=\"" + ImagesMap.getImages().get(Images.image4.name()).getFileName() + "\" alt=\"" + ImagesMap.getImages().get(Images.image4.name()).getAlt() + "\" " + imageSize + ">";
		}
	}
	
	public static class ImageData {
		private String sourcePath;
		private String sourceInPlugin;
		private String fileName;
		private String cid;
		private String alt;
		private int score;
		
		public ImageData(String sourcePath, String sourceInPlugin, String fileName, String cid, String alt, int score) {
			setSourcePath(sourcePath);
			setSourceInPlugin(sourceInPlugin);
			setFileName(fileName);
			setCid(cid);
			setAlt(alt);
			setScore(score);
		}
		
		public String getSourcePath() {
			return sourcePath;
		}
		
		public void setSourcePath(String sourcePath) {
			this.sourcePath = sourcePath;
		}
		
		public String getCid() {
			return cid;
		}
		
		public void setCid(String cid) {
			this.cid = cid;
		}
		
		public String getAlt() {
			return alt;
		}
		
		public void setAlt(String alt) {
			this.alt = alt;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public void setFileName(String htmlPath) {
			this.fileName = htmlPath;
		}
		
		public String getSourceInPlugin() {
			return sourceInPlugin;
		}
		
		public void setSourceInPlugin(String sourceInPlugin) {
			this.sourceInPlugin = sourceInPlugin;
		}
		
		public int getScore() {
			return score;
		}
		
		public void setScore(int score) {
			this.score = score;
		}
	}
}
