/******************************************************
 * 
 * Application Name:  Use AssemblyAI's free API.
 * Author:  John Steele
 * Date:  4/28/2024
 * Description:
 * This program makes use of the free API at assemblyAI.com.
 * An API is created here, then receives the response.
 * The first API should be to submit an audio file that assemblyai.com will transcribe.
 * The immediate response will be a confirmation or error about what was sent.
 * After some time (usually a few seconds to a few minutes), the transcribe will
 * be ready for download which this program will retrieve.
 * 
 *//////////////////////////////////////////////////////

package com.steele.api.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

public class AssemblyAI_API {

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

		String audio_url = "https://raw.githubusercontent.com/johnsteeleprogramming/JohnSteeleProgramming/main/AssemblyAI_audio_test_0.mp3";

		Transcript transcript = new Transcript();
		transcript.setAudio_url(audio_url);
		Gson gson = new Gson();
		String jsonRequest = gson.toJson(transcript);
		System.out.println("JSON:\n" + jsonRequest);

		String uri = "https://api.assemblyai.com/v2/transcript";
		String api_key = "60ee24660e50454d8da8841f0e1dc1f2";

		HttpRequest postRequest = HttpRequest.newBuilder()
				.uri(new URI(uri))
				.header("Authorization", api_key)
				.POST(BodyPublishers.ofString(jsonRequest))
				.build();

		printApi(postRequest);

		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> postResponse = httpClient.send(postRequest, BodyHandlers.ofString());
		System.out.println("Response Status Code: " + postResponse.statusCode());
		System.out.println("Response Body: " + postResponse.body());
		System.out.println("Response Body: " + postResponse.body());

		transcript = gson.fromJson(postResponse.body(), Transcript.class);
		System.out.println("Submission Status: " + transcript.getStatus());
		System.out.println("Submission ID: " + transcript.getId());
		System.out.println("GET URI: " + uri + "/" + transcript.getId());
		
		HttpRequest getRequest = HttpRequest.newBuilder()
				.uri(new URI(uri + "/" + transcript.getId()))
				.header("Authorization", api_key)
				.GET()
				.build();

		while (true) {
			HttpResponse<String> getResponse = httpClient.send(getRequest, BodyHandlers.ofString());
			transcript = gson.fromJson(getResponse.body(), Transcript.class);
			System.out.println("Status: " + transcript.getStatus());

			if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
				break;
			}
			Thread.sleep(1000);
		}

		System.out.println("Transcript completed");
		System.out.println("Text:\n" + transcript.getText());
		
	}

	public static void printApi(HttpRequest httpRequest) {
		String apiString = "";

		String method = "Method:\t" + httpRequest.method().toString();
		apiString = apiString.concat(method + "\n");

		String uri = "URI:\t" + httpRequest.uri().toString();
		apiString = apiString.concat(uri + "\n");

		apiString = apiString.concat("Headers:\n{\n");
		Map<String, List<String>> headers = httpRequest.headers().map();
		Set<String> keys = headers.keySet();
		for (String key : keys) {
			List<String> values = headers.get(key);
			for (String value : values) {
				apiString = apiString.concat("\t\"" + key + "\"\t: \"" + value + "\",\n");
			}
		}
		apiString = apiString.substring(0, apiString.length() - 2);
		apiString = apiString.concat("\n}\n");

		apiString = apiString.concat("Body:\n");
		String body = httpRequest.bodyPublisher().get().toString();
		apiString = apiString.concat(body + "\n");

		System.out.println(apiString);

	}
}
