import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const metric = await request.json();

    if (process.env.NEXT_PUBLIC_PERFORMANCE_MONITORING_ENABLED === 'true') {
      console.log('Performance Metric:', metric);
    }

    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error) {
    return NextResponse.json(
      { success: false, error: 'Invalid request' },
      { status: 400 }
    );
  }
}

