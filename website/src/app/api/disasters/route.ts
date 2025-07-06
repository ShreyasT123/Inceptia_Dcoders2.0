import { NextResponse } from 'next/server';

export async function GET(request: Request) {
  // Extract lat/lng from the client's request URL
  const { searchParams } = new URL(request.url);
  const lat = searchParams.get('lat');
  const lng = searchParams.get('lng');

  if (!lat || !lng) {
    return NextResponse.json({ error: 'Latitude and longitude parameters are required' }, { status: 400 });
  }

  // Securely get the API key from server environment variables
  const apiKey = "924b5b6c59a05b62b388568b225bdf9e66c28b8f2ed850de130728c700a75497";

  // This check is now extremely important.
  if (!apiKey) {
    console.error("CRITICAL: AMBEEDATA_API_KEY is missing from .env.local or server was not restarted.");
    return NextResponse.json({ error: 'API key not configured on the server' }, { status: 500 });
  }

  // Construct the correct AmbeeData API URL
  const ambeeUrl = `https://api.ambeedata.com/disasters/latest/by-lat-lng?lat=${lat}&lng=${lng}`;

  try {
    // Call the external API from the server
    const apiResponse = await fetch(ambeeUrl, {
      method: 'GET',
      headers: {
        'x-api-key': apiKey, // Use the key from .env.local
        'Content-type': 'application/json'
      },
    });

    // Get the response body regardless of success to provide better error details
    const responseBody = await apiResponse.json();

    if (!apiResponse.ok) {
      console.error("AmbeeData API Error:", responseBody);
      // Forward the error from AmbeeData to your frontend
      return NextResponse.json(
        { error: 'Failed to fetch data from AmbeeData', details: responseBody },
        { status: apiResponse.status }
      );
    }

    // Success: forward the data to your frontend
    return NextResponse.json(responseBody);

  } catch (error) {
    console.error("Internal Server Error in /api/disasters route:", error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}