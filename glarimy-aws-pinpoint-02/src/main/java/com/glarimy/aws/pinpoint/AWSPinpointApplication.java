package com.glarimy.aws.pinpoint;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.pinpoint.AmazonPinpoint;
import com.amazonaws.services.pinpoint.AmazonPinpointClientBuilder;
import com.amazonaws.services.pinpoint.model.Action;
import com.amazonaws.services.pinpoint.model.AttributeDimension;
import com.amazonaws.services.pinpoint.model.AttributeType;
import com.amazonaws.services.pinpoint.model.CreateAppRequest;
import com.amazonaws.services.pinpoint.model.CreateAppResult;
import com.amazonaws.services.pinpoint.model.CreateApplicationRequest;
import com.amazonaws.services.pinpoint.model.CreateCampaignRequest;
import com.amazonaws.services.pinpoint.model.CreateCampaignResult;
import com.amazonaws.services.pinpoint.model.CreateSegmentRequest;
import com.amazonaws.services.pinpoint.model.CreateSegmentResult;
import com.amazonaws.services.pinpoint.model.GCMChannelRequest;
import com.amazonaws.services.pinpoint.model.Message;
import com.amazonaws.services.pinpoint.model.MessageConfiguration;
import com.amazonaws.services.pinpoint.model.RecencyDimension;
import com.amazonaws.services.pinpoint.model.Schedule;
import com.amazonaws.services.pinpoint.model.SegmentBehaviors;
import com.amazonaws.services.pinpoint.model.SegmentDemographics;
import com.amazonaws.services.pinpoint.model.SegmentDimensions;
import com.amazonaws.services.pinpoint.model.SegmentLocation;
import com.amazonaws.services.pinpoint.model.UpdateGcmChannelRequest;
import com.amazonaws.services.pinpoint.model.UpdateGcmChannelResult;
import com.amazonaws.services.pinpoint.model.WriteCampaignRequest;
import com.amazonaws.services.pinpoint.model.WriteSegmentRequest;

public class AWSPinpointApplication {
	private String awsAccessKeyId;
	private String awsSecretAccessKey;
	private String googleApiKey;
	private String applicationName;
	private String applicationId;
	private String segmentId;
	private String campaignId;
	private AmazonPinpoint pinpoint;

	public void initializeAWSCredentials() {
		awsAccessKeyId = ""; // place your details here
		awsSecretAccessKey = ""; // place
									// your
									// details
									// here
		googleApiKey = ""; // place
							// your
							// goodle
							// api
							// key
							// here
	}

	public void createPinpointApplication(String name) {
		applicationName = name;
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
		pinpoint = AmazonPinpointClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCreds))
				.withRegion(Regions.US_EAST_1).build();
		CreateApplicationRequest appRequest = new CreateApplicationRequest().withName(applicationName);
		CreateAppRequest request = new CreateAppRequest();
		request.withCreateApplicationRequest(appRequest);
		CreateAppResult result = pinpoint.createApp(request);
		applicationId = result.getApplicationResponse().getId();
		System.out.println("Created Pinpoint Application Successfully!");
	}

	public void enableGCMChannel() {
		GCMChannelRequest request = new GCMChannelRequest().withEnabled(true).withApiKey(googleApiKey);
		UpdateGcmChannelRequest updateRequest = new UpdateGcmChannelRequest().withGCMChannelRequest(request)
				.withApplicationId(applicationId);
		UpdateGcmChannelResult updateResult = pinpoint.updateGcmChannel(updateRequest);
		if (updateResult.getGCMChannelResponse().getEnabled())
			System.out.println("GCM Channel is enabled");
		else
			System.out.println("GCM Channel is failed to be enabled");

	}

	public void createSegment() {
		Map<String, AttributeDimension> segmentAttributes = new HashMap<>();
		segmentAttributes.put("Team",
				new AttributeDimension().withAttributeType(AttributeType.INCLUSIVE).withValues("Lakers"));
		SegmentBehaviors segmentBehaviors = new SegmentBehaviors();
		SegmentDemographics segmentDemographics = new SegmentDemographics();
		SegmentLocation segmentLocation = new SegmentLocation();
		RecencyDimension recencyDimension = new RecencyDimension();
		recencyDimension.withDuration("DAY_30").withRecencyType("ACTIVE");
		segmentBehaviors.setRecency(recencyDimension);
		SegmentDimensions dimensions = new SegmentDimensions().withAttributes(segmentAttributes)
				.withBehavior(segmentBehaviors).withDemographic(segmentDemographics).withLocation(segmentLocation);
		WriteSegmentRequest writeSegmentRequest = new WriteSegmentRequest().withName("MySegment")
				.withDimensions(dimensions);
		CreateSegmentRequest createSegmentRequest = new CreateSegmentRequest().withApplicationId(applicationId)
				.withWriteSegmentRequest(writeSegmentRequest);
		CreateSegmentResult createSegmentResult = pinpoint.createSegment(createSegmentRequest);
		segmentId = createSegmentResult.getSegmentResponse().getId();
	}

	public void createCampaign(String url) {
		Schedule schedule = new Schedule().withStartTime("IMMEDIATE");

		Message gcmMessage = new Message().withAction(Action.DEEP_LINK).withBody("gcm body").withTitle("gcm title")
				.withUrl(url);
		MessageConfiguration gcmMessageConfiguration = new MessageConfiguration().withGCMMessage(gcmMessage);

		WriteCampaignRequest request = new WriteCampaignRequest().withDescription("My description.")
				.withSchedule(schedule).withSegmentId(segmentId).withName("MyCampaign")
				.withMessageConfiguration(gcmMessageConfiguration);

		CreateCampaignRequest createCampaignRequest = new CreateCampaignRequest().withApplicationId(applicationId)
				.withWriteCampaignRequest(request);
		CreateCampaignResult result = pinpoint.createCampaign(createCampaignRequest);
		campaignId = result.getCampaignResponse().getId();
	}

	public void showResults() {
		System.out.println("Application Name: " + applicationName);
		System.out.println("Application ID: " + applicationId);
		System.out.println("Segment ID: " + segmentId);
		System.out.println("Campaign ID: " + campaignId);
	}

	public static void main(String[] args) {
		AWSPinpointApplication app = new AWSPinpointApplication();

		app.initializeAWSCredentials();
		app.createPinpointApplication("Glarimy GCM Deeplink"); // give a name
																// for your
																// application
		app.enableGCMChannel();
		app.createSegment();
		app.createCampaign("myschema://mylink/page"); // specify the deep-link
														// url
		app.showResults();
	}
}
