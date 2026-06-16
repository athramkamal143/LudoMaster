import 'package:flutter/material.dart';
import '../models/admin_models.dart';
import '../services/mock_api_service.dart';

class CoinsView extends StatefulWidget {
  const CoinsView({Key? key}) : super(key: key);

  @override
  State<CoinsView> createState() => _CoinsViewState();
}

class _CoinsViewState extends State<CoinsView> {
  final MockApiService _apiService = MockApiService();
  
  // Local ledger transaction parameters
  String _targetUserId = "";
  int _auditAmount = 25000;
  bool _isCoinTransaction = true; // Coin or Gem toggle
  String _memo = "Daily administrative balance reconcile";

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final bool isWide = constraints.maxWidth > 960;
          return Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Left side: System circulation summary details & manual ledger tool
              Expanded(
                flex: 3,
                child: Column(
                  children: [
                    _buildOverviewCard(),
                    const SizedBox(height: 16),
                    Expanded(
                      child: Card(
                        child: Padding(
                          padding: const EdgeInsets.all(24),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              const Row(
                                children: [
                                  Icon(Icons.receipt_long, color: Color(0xFFCA8A04), size: 18),
                                  SizedBox(width: 8),
                                  Text(
                                    'ELECTRONIC TREASURY & TRANSACTION LOGS',
                                    style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 13),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 16),
                              Expanded(
                                child: ListenableBuilder(
                                  listenable: _apiService,
                                  builder: (context, _) {
                                    final coinAdjustLogs = _apiService.logs.where((log) => 
                                      log.action.contains("COIN") || log.action.contains("GEM")
                                    ).toList();

                                    if (coinAdjustLogs.isEmpty) {
                                      return const Center(child: Text("No treasury adjustments logged in this boot cycle."));
                                    }

                                    return ListView.separated(
                                      itemCount: coinAdjustLogs.length,
                                      separatorBuilder: (context, _) => const Divider(height: 1),
                                      itemBuilder: (context, index) {
                                        final log = coinAdjustLogs[index];
                                        return _buildTreasuryLogTile(log);
                                      },
                                    );
                                  },
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              if (isWide) ...[
                const SizedBox(width: 24),
                // Right side: Quick Direct Ledger Injection Panel
                Expanded(
                  flex: 2,
                  child: _buildDirectLedgerInjectionPanel(),
                ),
              ],
            ],
          );
        },
      ),
    );
  }

  Widget _buildOverviewCard() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'LUDO GOLD INFLATION INDEX',
              style: TextStyle(fontWeight: FontWeight.black, color: Colors.white24, fontSize: 11, letterSpacing: 0.8),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: _buildEconomyStatTile(
                    title: "TOTAL GOLD COINS IN CIRCULATION",
                    value: "🪙 ${_formatAmount(_apiService.systemTotalCoinCirculation)}",
                    color: const Color(0xFFCA8A04),
                    desc: "Accumulated cash wallets across global servers.",
                  ),
                ),
                const SizedBox(width: 24),
                Expanded(
                  child: _buildEconomyStatTile(
                    title: "TOTAL EMERALD GEMS REGISTERED",
                    value: "💎 ${_formatAmount(_apiService.systemTotalGemCirculation)}",
                    color: Colors.cyan,
                    desc: "Premium cash tokens circulation indices.",
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildEconomyStatTile({
    required String title,
    required String value,
    required Color color,
    required String desc,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF0C1322),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: const Color(0xFF1F2B44)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(fontSize: 10, color: Colors.white30, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          Text(value, style: TextStyle(fontWeight: FontWeight.black, fontSize: 20, color: color)),
          const SizedBox(height: 4),
          Text(desc, style: const TextStyle(fontSize: 10, color: Colors.white54)),
        ],
      ),
    );
  }

  Widget _buildTreasuryLogTile(AdminActivityLog log) {
    final bool isGem = log.action.contains("GEM");
    return ListTile(
      contentPadding: EdgeInsets.zero,
      leading: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: (isGem ? Colors.cyan : const Color(0xFFCA8A04)).withOpacity(0.08),
          shape: BoxShape.circle,
        ),
        child: Icon(
          isGem ? Icons.diamond : Icons.monetization_on,
          color: isGem ? Colors.cyan : const Color(0xFFCA8A04),
          size: 16,
        ),
      ),
      title: Text(log.details, style: const TextStyle(fontSize: 12.5, fontWeight: FontWeight.bold, color: Colors.white)),
      subtitle: Row(
        children: [
          Text('Admin ID: ${log.adminUser}', style: const TextStyle(fontSize: 10, color: Colors.white38)),
          const SizedBox(width: 12),
          Text(_formatTime(log.timestamp), style: const TextStyle(fontSize: 10, color: Colors.white34)),
        ],
      ),
      trailing: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color: Colors.green.withOpacity(0.08),
          borderRadius: BorderRadius.circular(4),
        ),
        child: const Text('SUCCESS', style: TextStyle(color: Colors.green, fontSize: 9, fontWeight: FontWeight.bold)),
      ),
    );
  }

  Widget _buildDirectLedgerInjectionPanel() {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.currency_exchange, color: Color(0xFFCA8A04), size: 18),
                SizedBox(width: 8),
                Text(
                  'DIRECT LEDGER INJECTION',
                  style: TextStyle(fontWeight: FontWeight.black, color: Colors.white, fontSize: 13),
                ),
              ],
            ),
            const SizedBox(height: 6),
            const Text(
              'Execute direct administrative wallet overrides on customer ledger rows.',
              style: TextStyle(fontSize: 11, color: Colors.white38),
            ),
            const Divider(),
            const SizedBox(height: 12),

            // Select Target Profile
            const Text('Database User Target', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
            const SizedBox(height: 6),
            ListenableBuilder(
              listenable: _apiService,
              builder: (context, _) {
                final userList = _apiService.users;
                if (_targetUserId.isEmpty && userList.isNotEmpty) {
                  _targetUserId = userList.first.id;
                }
                return DropdownButtonFormField<String>(
                  value: _targetUserId,
                  dropdownColor: const Color(0xFF131D31),
                  style: const TextStyle(fontSize: 13, color: Colors.white),
                  decoration: const InputDecoration(contentPadding: EdgeInsets.symmetric(horizontal: 10)),
                  items: userList.map((usr) {
                    return DropdownMenuItem(
                      value: usr.id,
                      child: Text("${usr.name} [${usr.id}]"),
                    );
                  }).toList(),
                  onChanged: (val) {
                    if (val != null) {
                      setState(() {
                        _targetUserId = val;
                      });
                    }
                  },
                );
              },
            ),
            const SizedBox(height: 16),

            // Select Currency
            const Text('Asset Class Mapping', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: InkWell(
                    onTap: () => setState(() => _isCoinTransaction = true),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        color: _isCoinTransaction ? const Color(0xFFCA8A04).withOpacity(0.1) : const Color(0xFF0C1322),
                        borderRadius: BorderRadius.circular(6),
                        border: Border.all(color: _isCoinTransaction ? const Color(0xFFCA8A04) : const Color(0xFF1F2B44)),
                      ),
                      alignment: Alignment.center,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.monetization_on, color: _isCoinTransaction ? const Color(0xFFCA8A04) : Colors.white24, size: 16),
                          const SizedBox(width: 8),
                          Text(
                            'Gold Coins',
                            style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: _isCoinTransaction ? Colors.white : Colors.white38),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: InkWell(
                    onTap: () => setState(() => _isCoinTransaction = false),
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      decoration: BoxDecoration(
                        color: !_isCoinTransaction ? Colors.cyan.withOpacity(0.1) : const Color(0xFF0C1322),
                        borderRadius: BorderRadius.circular(6),
                        border: Border.all(color: !_isCoinTransaction ? Colors.cyan : const Color(0xFF1F2B44)),
                      ),
                      alignment: Alignment.center,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.diamond, color: !_isCoinTransaction ? Colors.cyan : Colors.white24, size: 16),
                          const SizedBox(width: 8),
                          Text(
                            'Emerald Gems',
                            style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: !_isCoinTransaction ? Colors.white : Colors.white38),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Override Payout Amount
            const Text('Aggregate Target Balance Overwrite', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
            const SizedBox(height: 6),
            TextFormField(
              initialValue: "25000",
              style: const TextStyle(fontSize: 13),
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                prefixIcon: Icon(
                  _isCoinTransaction ? Icons.monetization_on : Icons.diamond,
                  color: _isCoinTransaction ? const Color(0xFFCA8A04) : Colors.cyan,
                  size: 16,
                ),
                labelText: _isCoinTransaction ? "New Coins balance amount" : "New Gems balance amount",
                labelStyle: const TextStyle(color: Colors.white38, fontSize: 12),
              ),
              onChanged: (val) {
                final parsed = int.tryParse(val) ?? 0;
                setState(() {
                  _auditAmount = parsed;
                });
              },
            ),
            const SizedBox(height: 16),

            // Audit Memo Log explanation
            const Text('Internal Auditor Memo Explanation', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.white60)),
            const SizedBox(height: 6),
            TextFormField(
              initialValue: _memo,
              style: const TextStyle(fontSize: 13),
              decoration: const InputDecoration(
                hintText: 'Audited log trace metadata notes...',
              ),
              onChanged: (val) => _memo = val,
            ),

            const Spacer(),

            ElevatedButton(
              onPressed: () {
                if (_targetUserId.isNotEmpty) {
                  if (_isCoinTransaction) {
                    _apiService.adjustUserCoins(_targetUserId, _auditAmount);
                  } else {
                    _apiService.adjustUserGems(_targetUserId, _auditAmount);
                  }
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Administrative Ledger injection completed.')),
                  );
                }
              },
              style: ElevatedButton.styleFrom(
                backgroundColor: _isCoinTransaction ? const Color(0xFFCA8A04) : Colors.cyan,
                foregroundColor: Colors.black,
                minimumSize: const Size.fromHeight(48),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.security, size: 18),
                  const SizedBox(width: 8),
                  Text(
                    'EXECUTE LEDGER INJECTION',
                    style: TextStyle(fontWeight: FontWeight.black, fontSize: 13, color: _isCoinTransaction ? Colors.black : Colors.black87),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _formatAmount(int amt) {
    final String s = amt.toString();
    final RegExp reg = RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))');
    return s.replaceAllMapped(reg, (Match m) => '${m[1]},');
  }

  String _formatTime(DateTime dt) {
    return "${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}:${dt.second.toString().padLeft(2, '0')}";
  }
}
