import 'package:flutter/material.dart';
import '../services/mock_api_service.dart';
import 'analytics_view.dart';
import 'users_view.dart';
import 'tournaments_view.dart';
import 'coins_view.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({Key? key}) : super(key: key);

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  int _selectedTabIndex = 0;
  final MockApiService _apiService = MockApiService();

  @override
  Widget build(BuildContext context) {
    final double screenWidth = MediaQuery.of(context).size.width;
    final bool isDesktop = screenWidth >= 1024;
    final bool isTablet = screenWidth >= 640 && screenWidth < 1024;

    return ListenableBuilder(
      listenable: _apiService,
      builder: (context, _) {
        return Scaffold(
          appBar: isDesktop
              ? null
              : AppBar(
                  backgroundColor: const Color(0xFF131D31),
                  title: Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(6),
                        decoration: BoxDecoration(
                          color: const Color(0xFFCA8A04).withOpacity(0.15),
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.stars, color: Color(0xFFCA8A04), size: 18),
                      ),
                      const SizedBox(width: 8),
                      const Text(
                        'LUDO ROYALE - ADMIN',
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.black,
                          letterSpacing: 1,
                        ),
                      ),
                    ],
                  ),
                  actions: [
                    _buildAdminIndicator(),
                    const SizedBox(width: 12),
                  ],
                ),
          drawer: isDesktop ? null : _buildSidebar(context, false),
          body: Row(
            children: [
              if (isDesktop) _buildSidebar(context, true),
              Expanded(
                child: Column(
                  children: [
                    if (isDesktop) _buildTopHeaderNav(),
                    Expanded(
                      child: ClipRect(
                        child: AnimatedSwitcher(
                          duration: const Duration(milliseconds: 250),
                          child: _getSelectedPage(_selectedTabIndex),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  // Header Nav for desktops
  Widget _buildTopHeaderNav() {
    return Container(
      height: 70,
      padding: const EdgeInsets.symmetric(horizontal: 24),
      decoration: const BoxDecoration(
        color: Color(0xFF131D31),
        border: Border(
          bottom: BorderSide(color: Color(0xFF1F2B44), width: 1.2),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              const Icon(Icons.dashboard_customize, color: Colors.white54, size: 20),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    _getTabTitle(_selectedTabIndex),
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.black,
                      letterSpacing: 0.5,
                      color: Colors.white,
                    ),
                  ),
                  Text(
                    _getTabSubtitle(_selectedTabIndex),
                    style: TextStyle(
                      fontSize: 11,
                      color: Colors.white.copy(0.5),
                    ),
                  ),
                ],
              ),
            ],
          ),
          Row(
            children: [
              _buildStatsChip(
                icon: Icons.people_outline,
                color: Colors.blueAccent,
                label: 'Active: ${_apiService.activeUsersCount}',
              ),
              const SizedBox(width: 12),
              _buildStatsChip(
                icon: Icons.monetization_on_outlined,
                color: const Color(0xFFCA8A04),
                label: 'Circulation: ${_formatCompact(_apiService.systemTotalCoinCirculation)}',
              ),
              const SizedBox(width: 24),
              _buildAdminIndicator(),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatsChip({
    required IconData icon,
    required Color color,
    required String label,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.08),
        borderRadius: BorderRadius.circular(20),
        border: Border.solid(color: color.withOpacity(0.18), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color, size: 14),
          const SizedBox(width: 6),
          Text(
            label,
            style: TextStyle(
              fontSize: 11,
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAdminIndicator() {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text(
              'Athram Kamal',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.white),
            ),
            Text(
              'Root Super Admin',
              style: TextStyle(color: const Color(0xFFCA8A04).withOpacity(0.8), fontSize: 10, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(width: 10),
        Container(
          width: 38,
          height: 38,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: const LinearGradient(
              colors: [Color(0xFFCA8A04), Color(0xFFB59410)],
            ),
            border: Border.all(color: Colors.white.withOpacity(0.2), width: 2),
          ),
          child: const Center(
            child: Text(
              'AK',
              style: TextStyle(fontWeight: FontWeight.black, fontSize: 13, color: Colors.black),
            ),
          ),
        ),
      ],
    );
  }

  // Collapsible / Desktop sidebar
  Widget _buildSidebar(BuildContext context, bool isDesktop) {
    return Container(
      width: 260,
      color: const Color(0xFF0C1322),
      border: const Border(
        right: BorderSide(color: Color(0xFF1F2B44), width: 1.2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Logo Section
          Container(
            padding: const EdgeInsets.all(24),
            decoration: const BoxDecoration(
              border: Border(
                bottom: BorderSide(color: Color(0xFF1F2B44), width: 1),
              ),
            ),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    gradient: const LinearGradient(
                      colors: [Color(0xFFCA8A04), Color(0xFFFFD700)],
                    ),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Icon(Icons.stars, color: Colors.black, size: 22),
                ),
                const SizedBox(width: 12),
                const Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'LUDO ROYALE',
                      style: TextStyle(
                        fontWeight: FontWeight.black,
                        color: Colors.white,
                        letterSpacing: 1.2,
                        fontSize: 15,
                      ),
                    ),
                    Text(
                      'WEB ADMIN SYSTEM',
                      style: TextStyle(
                        fontSize: 9,
                        color: Color(0xFFCA8A04),
                        fontWeight: FontWeight.bold,
                        letterSpacing: 2,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),
          const Padding(
            padding: EdgeInsets.symmetric(horizontal: 24, vertical: 8),
            child: Text(
              'NUCLEUS SYSTEMS',
              style: TextStyle(
                fontSize: 10,
                color: Colors.white30,
                fontWeight: FontWeight.black,
                letterSpacing: 1.5,
              ),
            ),
          ),

          // Menu Navigation Tabs
          _buildSidebarNavItem(
            index: 0,
            icon: Icons.analytics_outlined,
            label: 'Analytics Dashboard',
          ),
          _buildSidebarNavItem(
            index: 1,
            icon: Icons.gavel_rounded,
            label: 'User Management',
          ),
          _buildSidebarNavItem(
            index: 2,
            icon: Icons.emoji_events_outlined,
            label: 'Tournaments Campaign',
          ),
          _buildSidebarNavItem(
            index: 3,
            icon: Icons.payments_outlined,
            label: 'Coin & Gems Ledger',
          ),
          _buildSidebarNavItem(
            index: 4,
            icon: Icons.history,
            label: 'Global Audit Logs',
          ),

          const Spacer(),
          // Build details
          Container(
            padding: const EdgeInsets.all(20),
            color: const Color(0xFF090F1D),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      decoration: const BoxDecoration(
                        color: Colors.green,
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      'NODE WEBSOCKET LIVE',
                      style: TextStyle(fontSize: 10, color: Colors.white54, fontWeight: FontWeight.bold),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                const Text(
                  'Console version: v1.8.2-Prod\nAthram Kamal © 2026',
                  style: TextStyle(fontSize: 9, color: Colors.white24),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSidebarNavItem({
    required int index,
    required IconData icon,
    required String label,
  }) {
    final bool isSelected = _selectedTabIndex == index;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: InkWell(
        onTap: () {
          setState(() {
            _selectedTabIndex = index;
          });
          if (Scaffold.of(context).isDrawerOpen) {
            Navigator.pop(context); // Close Drawer
          }
        },
        borderRadius: BorderRadius.circular(8),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          decoration: BoxDecoration(
            color: isSelected ? const Color(0xFFCA8A04).withOpacity(0.12) : Colors.transparent,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(
              color: isSelected ? const Color(0xFFCA8A04).withOpacity(0.3) : Colors.transparent,
              width: 1,
            ),
          ),
          child: Row(
            children: [
              Icon(
                icon,
                color: isSelected ? const Color(0xFFCA8A04) : Colors.white60,
                size: 18,
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Text(
                  label,
                  style: TextStyle(
                    color: isSelected ? Colors.white : Colors.white70,
                    fontWeight: isSelected ? FontWeight.black : FontWeight.normal,
                    fontSize: 13,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // Screen mapping router
  Widget _getSelectedPage(int index) {
    switch (index) {
      case 0:
        return const AnalyticsView();
      case 1:
        return const UsersView();
      case 2:
        return const TournamentsView();
      case 3:
        return const CoinsView();
      case 4:
        return const AuditLogsView();
      default:
        return const AnalyticsView();
    }
  }

  String _getTabTitle(int index) {
    switch (index) {
      case 0:
        return "ANALYTICS COMMAND DECK";
      case 1:
        return "USER BASE LEDGER MANAGEMENT";
      case 2:
        return "TOURNAMENT SCHEDULING ROOM";
      case 3:
        return "ECONOMY CURRENCY AUDIT PANEL";
      case 4:
        return "GLOBAL AUDIT SYSTEM LOGS";
      default:
        return "ADMIN DASHBOARD";
    }
  }

  String _getTabSubtitle(int index) {
    switch (index) {
      case 0:
        return "Overview of active session telemetry, retention curves and currency distributions.";
      case 1:
        return "Review active profiles, ban malicious behavior, configure custom VIP traits.";
      case 2:
        return "Construct active championship lobbies, configure registration entry fees.";
      case 3:
        return "Execute safe bulk coin adjustments, audit manual balance ledger histories.";
      case 4:
        return "Cryptographically signed trace records of all admin actions.";
      default:
        return "Configure your Ludo Royale server node states.";
    }
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

// Simple Composable for logs since its code is concise
class AuditLogsView extends StatelessWidget {
  const AuditLogsView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final MockApiService apiService = MockApiService();

    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'CRYPTOGRAPHIC AUDIT TRAIL',
                    style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 14),
                  ),
                  _buildPulseChip(),
                ],
              ),
              const SizedBox(height: 16),
              Expanded(
                child: ListenableBuilder(
                  listenable: apiService,
                  builder: (context, _) {
                    final logsList = apiService.logs;
                    if (logsList.isEmpty) {
                      return const Center(child: Text("No audit events recorded yet."));
                    }
                    return ListView.builder(
                      itemCount: logsList.length,
                      itemBuilder: (context, index) {
                        final log = logsList[index];
                        return _buildLogTile(log);
                      },
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildPulseChip() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.green.withOpacity(0.08),
        borderRadius: BorderRadius.circular(6),
        border: Border.all(color: Colors.green.withOpacity(0.2)),
      ),
      child: const Row(
        children: [
          Icon(Icons.security, color: Colors.green, size: 12),
          SizedBox(width: 6),
          Text(
            "TRACE ACTIVE",
            style: TextStyle(color: Colors.green, fontSize: 9, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildLogTile(AdminActivityLog log) {
    Color actionColor = const Color(0xFFCA8A04);
    if (log.action.contains("BAN")) {
      actionColor = Colors.red;
    } else if (log.action.contains("CREATE")) {
      actionColor = Colors.blue;
    } else if (log.action.contains("UNBAN")) {
      actionColor = Colors.green;
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF0C1322),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFF1F2B44), width: 0.8),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: actionColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: actionColor.withOpacity(0.3)),
            ),
            child: Text(
              log.action,
              style: TextStyle(color: actionColor, fontSize: 10, fontWeight: FontWeight.black),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  log.details,
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12.5),
                ),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Text(
                      'Target: ${log.target}',
                      style: const TextStyle(color: Colors.white54, fontSize: 10),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      'Admin: ${log.adminUser}',
                      style: const TextStyle(color: Colors.white54, fontSize: 10),
                    ),
                    const SizedBox(width: 12),
                    Text(
                      _formatDate(log.timestamp),
                      style: const TextStyle(color: Colors.white30, fontSize: 10),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _formatDate(DateTime dt) {
    return "${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}:${dt.second.toString().padLeft(2, '0')}";
  }
}
