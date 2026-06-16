import 'package:flutter/material.dart';
import '../services/mock_api_service.dart';

class AnalyticsView extends StatelessWidget {
  const AnalyticsView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final MockApiService apiService = MockApiService();

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Stat grid
          LayoutBuilder(
            builder: (context, constraints) {
              final double cardWidth = (constraints.maxWidth - 48) / (constraints.maxWidth > 1024 ? 4 : (constraints.maxWidth > 640 ? 2 : 1));
              return Wrap(
                spacing: 16,
                runSpacing: 16,
                children: [
                  SizedBox(
                    width: cardWidth,
                    child: _buildSummaryCard(
                      title: "TOTAL USER PENETRATION",
                      value: "${apiService.totalUsersCount}",
                      subtitle: "Registered profiles across node databases",
                      icon: Icons.people,
                      color: Colors.blueAccent,
                      badge: "+14.2% MoM",
                    ),
                  ),
                  SizedBox(
                    width: cardWidth,
                    child: _buildSummaryCard(
                      title: "VIP ROYALE CLUB MEMBERS",
                      value: "${apiService.vipUsersCount}",
                      subtitle: "VIP subscription state holders",
                      icon: Icons.stars,
                      color: const Color(0xFFCA8A04),
                      badge: "40.0% Ratio",
                    ),
                  ),
                  SizedBox(
                    width: cardWidth,
                    child: _buildSummaryCard(
                      title: "TOTAL COIN CIRCULATION",
                      value: _formatCompact(apiService.systemTotalCoinCirculation),
                      subtitle: "Aggregated wallet system gold ledger",
                      icon: Icons.monetization_on,
                      color: Colors.green,
                      badge: "Balanced Status",
                    ),
                  ),
                  SizedBox(
                    width: cardWidth,
                    child: _buildSummaryCard(
                      title: "BANNED CHEAT DETECTION",
                      value: "${apiService.bannedUsersCount}",
                      subtitle: "Anti-fraud and collision auto-banned",
                      icon: Icons.gavel,
                      color: Colors.red,
                      badge: "Secure Matrix",
                    ),
                  ),
                ],
              );
            },
          ),
          const SizedBox(height: 24),

          // Core visual charts row
          LayoutBuilder(
            builder: (context, constraints) {
              if (constraints.maxWidth > 1024) {
                return Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(flex: 3, child: _buildRetentionChart()),
                    const SizedBox(width: 24),
                    Expanded(flex: 2, child: _buildActiveLobbySessions()),
                  ],
                );
              } else {
                return Column(
                  children: [
                    _buildRetentionChart(),
                    const SizedBox(height: 24),
                    _buildActiveLobbySessions(),
                  ],
                );
              }
            },
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryCard({
    required String title,
    required String value,
    required String subtitle,
    required IconData icon,
    required Color color,
    required String badge,
  }) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  title,
                  style: const TextStyle(fontWeight: FontWeight.black, color: Colors.white24, fontSize: 10, letterSpacing: 1),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: color.withOpacity(0.08),
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    badge,
                    style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Icon(icon, color: color, size: 28),
                const SizedBox(width: 12),
                Text(
                  value,
                  style: const TextStyle(fontWeight: FontWeight.black, fontSize: 26, color: Colors.white, letterSpacing: -0.5),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              subtitle,
              style: const TextStyle(color: Colors.white38, fontSize: 11),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRetentionChart() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'CONCURRENT DAU PLAYER TELEMETRY',
                      style: TextStyle(fontWeight: FontWeight.black, color: Colors.white),
                    ),
                    Text(
                      'Hourly distribution showing Peak Concurrencies and Bot Engagement ratios.',
                      style: TextStyle(fontSize: 11, color: Colors.white38),
                    ),
                  ],
                ),
                _buildLiveTelemetryChip(),
              ],
            ),
            const SizedBox(height: 24),
            // Custom painter chart block
            Container(
              height: 220,
              padding: const EdgeInsets.only(top: 10, right: 10),
              child: CustomPaint(
                size: Size.infinite,
                painter: LudoChartPainter(),
              ),
            ),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                _buildLegendItem("Human Concurrent Players", Colors.orange),
                const SizedBox(width: 24),
                _buildLegendItem("Smart Bot Ensembles", const Color(0xFFCA8A04)),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLiveTelemetryChip() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFCA8A04).withOpacity(0.08),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: const Color(0xFFCA8A04).withOpacity(0.2)),
      ),
      child: const Row(
        children: [
          Icon(Icons.wifi_tethering, color: Color(0xFFCA8A04), size: 12),
          SizedBox(width: 6),
          Text(
            "LIVE 2 SEC REFRESH",
            style: TextStyle(color: Color(0xFFCA8A04), fontSize: 9, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem(String label, Color color) {
    return Row(
      children: [
        Container(
          width: 10,
          height: 10,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 8),
        Text(
          label,
          style: const TextStyle(fontSize: 11, color: Colors.white54, fontWeight: FontWeight.bold),
        ),
      ],
    );
  }

  Widget _buildActiveLobbySessions() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'ACTIVE NODE CAMPAIGNS',
              style: TextStyle(fontWeight: FontWeight.black, color: Colors.white),
            ),
            const Text(
              'Lobby queue mapping on master-region nodes.',
              style: TextStyle(fontSize: 11, color: Colors.white38),
            ),
            const SizedBox(height: 20),
            _buildServerRow("Asia Mumbai Node-1", "76 Rooms", "Online", Colors.green),
            _buildServerRow("Europe Frankfurt Node-2", "42 Rooms", "Online", Colors.green),
            _buildServerRow("US East Virginia Node-3", "12 Rooms", "Maintenance", Colors.orange),
            _buildServerRow("South America Sao Paulo Node-4", "0 Rooms", "Offline", Colors.red),
          ],
        ),
      ),
    );
  }

  Widget _buildServerRow(String server, String rooms, String status, Color statusColor) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF0C1322),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(server, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.white)),
              const SizedBox(height: 2),
              Text(rooms, style: const TextStyle(fontSize: 11, color: Colors.white38)),
            ],
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: statusColor.withOpacity(0.08),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(color: statusColor.withOpacity(0.18)),
            ),
            child: Text(
              status.toUpperCase(),
              style: TextStyle(color: statusColor, fontSize: 10, fontWeight: FontWeight.black),
            ),
          ),
        ],
      ),
    );
  }

  String _formatCompact(int value) {
    if (value >= 1000000) {
      return "${(value / 1000000).toStringAsFixed(1)}M";
    } else if (value >= 1000) {
      return "${(value / 1000).toStringAsFixed(1)}K";
    }
    return value.toString();
  }
}

// Gorgeous High-Fidelity Custom Canvas Painter for Charts to guarantee seamless, crash-free performance!
class LudoChartPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final Paint linePaint1 = Paint()
      ..color = Colors.orange
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3.2
      ..strokeCap = StrokeCap.round;

    final Paint linePaint2 = Paint()
      ..color = const Color(0xFFCA8A04)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.4
      ..strokeCap = StrokeCap.round;

    final Paint axisPaint = Paint()
      ..color = const Color(0xFF1F2B44)
      ..strokeWidth = 1.0;

    final double w = size.width;
    final double h = size.height;

    // Draw Grid Lines
    for (int i = 0; i <= 4; i++) {
      final double yPos = h - (h / 4) * i;
      canvas.drawLine(Offset(0, yPos), Offset(w, yPos), axisPaint);
    }

    // Coordinates points setup
    final List<Offset> points1 = [
      Offset(0, h * 0.8),
      Offset(w * 0.16, h * 0.72),
      Offset(w * 0.33, h * 0.55),
      Offset(w * 0.5, h * 0.42),
      Offset(w * 0.66, h * 0.15), // Peak
      Offset(w * 0.83, h * 0.38),
      Offset(w, h * 0.28),
    ];

    final List<Offset> points2 = [
      Offset(0, h * 0.9),
      Offset(w * 0.16, h * 0.85),
      Offset(w * 0.33, h * 0.7),
      Offset(w * 0.5, h * 0.61),
      Offset(w * 0.66, h * 0.45),
      Offset(w * 0.83, h * 0.52),
      Offset(w, h * 0.48),
    ];

    // Draw Smooth Line 1
    final Path path1 = Path()..moveTo(points1[0].dx, points1[0].dy);
    for (int i = 1; i < points1.length; i++) {
      final double xc = (points1[i - 1].dx + points1[i].dx) / 2;
      final double yc = (points1[i - 1].dy + points1[i].dy) / 2;
      path1.quadraticBezierTo(points1[i - 1].dx, points1[i - 1].dy, xc, yc);
    }
    path1.lineTo(points1.last.dx, points1.last.dy);
    canvas.drawPath(path1, linePaint1);

    // Draw Smooth Line 2
    final Path path2 = Path()..moveTo(points2[0].dx, points2[0].dy);
    for (int i = 1; i < points2.length; i++) {
      final double xc = (points2[i - 1].dx + points2[i].dx) / 2;
      final double yc = (points2[i - 1].dy + points2[i].dy) / 2;
      path2.quadraticBezierTo(points2[i - 1].dx, points2[i - 1].dy, xc, yc);
    }
    path2.lineTo(points2.last.dx, points2.last.dy);
    canvas.drawPath(path2, linePaint2);

    // Draw Highlight Point Overlay on Line 1's Peak
    final Paint peakOvalPaint = Paint()..color = Colors.orange;
    final Paint peakRingPaint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0;

    canvas.drawCircle(points1[4], 6.0, peakOvalPaint);
    canvas.drawCircle(points1[4], 6.0, peakRingPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
