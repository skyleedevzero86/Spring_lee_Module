import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const errorReport = await request.json();

    if (process.env.NEXT_PUBLIC_ERROR_TRACKING_ENABLED === 'true') {
      console.error('Error Report:', errorReport);
    }

    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error) {
    return NextResponse.json(
      { success: false, error: 'Invalid request' },
      { status: 400 }
    );
  }
}

