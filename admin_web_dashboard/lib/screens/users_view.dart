import 'package:flutter/material.dart';
import '../models/admin_models.dart';
import '../services/mock_api_service.dart';

class UsersView extends StatefulWidget {
  const UsersView({Key? key}) : super(key: key);

  @override
  State<UsersView> createState() => _UsersViewState();
}

class _UsersViewState extends State<UsersView> {
  final MockApiService _apiService = MockApiService();
  String _searchQuery = "";
  String _filterStatus = "ALL"; // ALL, ACTIVE, BANNED, VIP

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Search & Filter header
              _buildControlFilters(),
              const SizedBox(height: 20),

              // Users Table list
              Expanded(
                child: ListenableBuilder(
                  listenable: _apiService,
                  builder: (context, _) {
                    final filteredUsers = _apiService.users.where((user) {
                      final matchesSearch = user.name.toLowerCase().contains(_searchQuery.toLowerCase()) || 
                                           user.email.toLowerCase().contains(_searchQuery.toLowerCase()) ||
                                           user.id.toLowerCase().contains(_searchQuery.toLowerCase());
                      
                      final matchesStatus = _filterStatus == "ALL" ||
                                            (_filterStatus == "ACTIVE" && user.status == UserStatus.active) ||
                                            (_filterStatus == "BANNED" && user.status == UserStatus.banned) ||
                                            (_filterStatus == "VIP" && user.isVip);

                      return matchesSearch && matchesStatus;
                    }).toList();

                    if (filteredUsers.isEmpty) {
                      return const Center(child: Text("No user matches the current filters."));
                    }

                    return ListView.separated(
                      itemCount: filteredUsers.length,
                      separatorBuilder: (context, _) => const Divider(height: 1.0),
                      itemBuilder: (context, index) {
                        final user = filteredUsers[index];
                        return _buildUserTableRow(user);
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

  Widget _buildControlFilters() {
    return Row(
      children: [
        // Search Box
        Expanded(
          child: TextField(
            onChanged: (val) {
              setState(() {
                _searchQuery = val;
              });
            },
            decoration: InputDecoration(
              hintText: 'Search by Player name, email or ID...',
              hintStyle: const TextStyle(color: Colors.white24, fontSize: 13),
              prefixIcon: const Icon(Icons.search, color: Colors.white54, size: 18),
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            ),
          ),
        ),
        const SizedBox(width: 16),
        // Dropdown status filter
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12),
          decoration: BoxDecoration(
            color: const Color(0xFF0C1322),
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: const Color(0xFF1F2B44)),
          ),
          child: DropdownButtonHideUnderline(
            child: DropdownButton<String>(
              value: _filterStatus,
              dropdownColor: const Color(0xFF131D31),
              onChanged: (val) {
                if (val != null) {
                  setState(() {
                    _filterStatus = val;
                  });
                }
              },
              style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 13),
              items: const [
                DropdownMenuItem(value: "ALL", child: Text("All Profiles")),
                DropdownMenuItem(value: "ACTIVE", child: Text("Active Users")),
                DropdownMenuItem(value: "BANNED", child: Text("Banned / Flagged")),
                DropdownMenuItem(value: "VIP", child: Text("VIP Members")),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildUserTableRow(LudoUser user) {
    final bool isBanned = user.status == UserStatus.banned;
    final Color statusColor = isBanned ? Colors.red : Colors.green;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 14.0),
      child: Row(
        children: [
          // Circular Avatar representation
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Color(int.parse(user.avatarColor)),
              border: Border.all(
                color: user.isVip ? const Color(0xFFCA8A04) : Colors.transparent,
                width: 2,
              ),
            ),
            child: Center(
              child: Text(
                user.name.substring(0, 1).toUpperCase(),
                style: const TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 15),
              ),
            ),
          ),
          const SizedBox(width: 16),

          // User naming details
          Expanded(
            flex: 3,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Text(
                      user.name,
                      style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 14),
                    ),
                    if (user.isVip) ...[
                      const SizedBox(width: 8),
                      const Tooltip(
                        message: "VIP Royale Member",
                        child: Icon(Icons.stars, color: Color(0xFFCA8A04), size: 14),
                      ),
                    ],
                  ],
                ),
                Text(
                  'ID: ${user.id}  •  ${user.email}',
                  style: const TextStyle(color: Colors.white38, fontSize: 11),
                ),
              ],
            ),
          ),

          // Stats columns
          Expanded(
            flex: 2,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '🪙 ${_formatCompact(user.coins)} Coins',
                  style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
                ),
                Text(
                  '💎 ${user.gems} Gems',
                  style: const TextStyle(color: Colors.white54, fontSize: 11),
                ),
              ],
            ),
          ),

          // Matches & Win rates
          Expanded(
            flex: 2,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${user.matchesPlayCount} Matches Played',
                  style: const TextStyle(color: Colors.white70, fontSize: 12),
                ),
                Text(
                  'Win Rate: ${(user.winRatio * 100).toStringAsFixed(0)}%',
                  style: const TextStyle(color: Colors.white38, fontSize: 11),
                ),
              ],
            ),
          ),

          // Status Badge
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
            decoration: BoxDecoration(
              color: statusColor.withOpacity(0.08),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(color: statusColor.withOpacity(0.18)),
            ),
            child: Text(
              user.status.toString().split('.').last.toUpperCase(),
              style: TextStyle(color: statusColor, fontSize: 10, fontWeight: FontWeight.black, letterSpacing: 0.5),
            ),
          ),
          const SizedBox(width: 24),

          // Action row trigger button
          ElevatedButton(
            onPressed: () {
              _showUserDetailManagementDialog(context, user);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFF0C1322),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              shape: RoundedCornerShape(6),
              side: const BorderSide(color: Color(0xFF1F2B44)),
            ),
            child: const Text('Manage', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
          ),
        ],
      ),
    );
  }

  // Large Modal Screen detailed action sheet
  void _showUserDetailManagementDialog(BuildContext context, LudoUser user) {
    final TextEditingController coinController = TextEditingController(text: user.coins.toString());
    final TextEditingController gemsController = TextEditingController(text: user.gems.toString());

    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setModalState) {
            final bool isBanned = user.status == UserStatus.banned;
            
            return AlertDialog(
              backgroundColor: const Color(0xFF131D31),
              shape: RoundedCornerShape(16),
              title: Row(
                children: [
                  Container(
                    width: 10,
                    height: 10,
                    decoration: BoxDecoration(
                      color: isBanned ? Colors.red : Colors.green,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 10),
                  Text('Profiles Controls • ${user.name}'),
                ],
              ),
              content: SizedBox(
                width: 480,
                child: SingleChildScrollView(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      // Subheading Metadata
                      _buildModalMetadataRow("REGISTRATION DATE", _formatDateTime(user.registrationDate)),
                      _buildModalMetadataRow("ACCOUNT UNIQUE KEY ID", user.id),
                      if (user.isVip) ...[
                        _buildModalMetadataRow("REGULATED DICE STYLE", user.vipDice),
                        _buildModalMetadataRow("EQUIPPED CROWN FRAME", user.vipFrame),
                      ],
                      const Divider(),
                      const SizedBox(height: 12),

                      // Currencies Admin Controls Header
                      const Text(
                        "COIN LEDGER & GEMS INJECTIONS",
                        style: TextStyle(fontWeight: FontWeight.black, color: Colors.white60, fontSize: 11),
                      ),
                      const SizedBox(height: 10),
                      Row(
                        children: [
                          Expanded(
                            child: TextField(
                              controller: coinController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: "Gold Balance (Coins)",
                                labelStyle: TextStyle(color: Colors.white38),
                                prefixIcon: Icon(Icons.monetization_on, color: Color(0xFFCA8A04), size: 16),
                              ),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: TextField(
                              controller: gemsController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: "Emerald Gems Balance",
                                labelStyle: TextStyle(color: Colors.white38),
                                prefixIcon: Icon(Icons.diamond, color: Colors.cyan, size: 16),
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          TextButton(
                            onPressed: () {
                              final int? c = int.tryParse(coinController.text);
                              final int? g = int.tryParse(gemsController.text);
                              if (c != null) {
                                _apiService.adjustUserCoins(user.id, c);
                              }
                              if (g != null) {
                                _apiService.adjustUserGems(user.id, g);
                              }
                              Navigator.pop(context);
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(content: Text("Wallet balances updated successfully!")),
                              );
                            },
                            child: const Text("Apply Wallet Override", style: TextStyle(fontWeight: FontWeight.bold, color: Color(0xFFCA8A04))),
                          ),
                        ],
                      ),
                      const Divider(),
                      const SizedBox(height: 12),

                      // Ban State System control card
                      const Text(
                        "CRITICAL FRAUD & SUSPENSION ENGINES",
                        style: TextStyle(fontWeight: FontWeight.black, color: Colors.white60, fontSize: 11),
                      ),
                      const SizedBox(height: 10),
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: isBanned ? Colors.red.withOpacity(0.06) : Colors.green.withOpacity(0.06),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(
                            color: isBanned ? Colors.red.withOpacity(0.2) : Colors.green.withOpacity(0.2),
                          ),
                        ),
                        child: Row(
                          children: [
                            Icon(
                              isBanned ? Icons.gavel : Icons.verified_user,
                              color: isBanned ? Colors.red : Colors.green,
                              size: 22,
                            ),
                            const SizedBox(width: 14),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    isBanned ? "Account Flagged & Suspended" : "Account Active & Sound",
                                    style: TextStyle(fontWeight: FontWeight.black, color: isBanned ? Colors.red : Colors.green, fontSize: 13),
                                  ),
                                  Text(
                                    isBanned 
                                        ? "Banned players cannot joint match lobbies, play passes or chat in public."
                                        : "This account has sound standing in the matchmaking nodes.",
                                    style: const TextStyle(fontSize: 10, color: Colors.white54, height: 1.3),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 12),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          ElevatedButton(
                            onPressed: () {
                              _apiService.toggleUserBan(user.id);
                              // Refresh modal state
                              setModalState(() {});
                            },
                            style: ElevatedButton.styleFrom(
                              backgroundColor: isBanned ? Colors.green : Colors.red,
                              foregroundColor: Colors.black,
                              shape: RoundedCornerShape(6),
                            ),
                            child: Text(
                              isBanned ? "UNBAN USER PROFILE" : "SUSPEND / BAN PLAYER",
                              style: const TextStyle(fontWeight: FontWeight.black, fontSize: 11),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: const Text('Back', style: TextStyle(color: Colors.white54)),
                ),
              ],
            );
          },
        );
      },
    );
  }

  Widget _buildModalMetadataRow(String key, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(key, style: const TextStyle(fontSize: 10, color: Colors.white30, fontWeight: FontWeight.bold)),
          Text(value, style: const TextStyle(fontSize: 11, color: Colors.white)),
        ],
      ),
    );
  }

  String _formatDateTime(DateTime dt) {
    return "${dt.day}/${dt.month}/${dt.year}";
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

// Custom shape helper for layouts
RoundedRectangleBorder RoundedCornerShape(double radius) {
  return RoundedRectangleBorder(borderRadius: BorderRadius.circular(radius));
}
