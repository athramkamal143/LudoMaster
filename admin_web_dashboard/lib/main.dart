import 'package:flutter/material.dart';
import 'services/mock_api_service.dart';
import 'screens/dashboard_screen.dart';

void main() {
  runApp(const LudoAdminDashboardApp());
}

class LudoAdminDashboardApp extends StatelessWidget {
  const LudoAdminDashboardApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Ludo Royale - Admin Web Console',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFFCA8A04), // Elegant Ludo Gold
          secondary: Color(0xFF3B82F6), // Blue
          background: Color(0xFF090F1D), // Ludo Navy
          surface: Color(0xFF131D31), // Card Navy
          onBackground: Colors.white,
          onSurface: Colors.white,
          error: Color(0xFFEF4444), // Crimson Red
        ),
        scaffoldBackgroundColor: const Color(0xFF090F1D),
        cardTheme: CardTheme(
          color: const Color(0xFF131D31),
          elevation: 0,
          shape: RoundedCornerShape(12),
          side: const BorderSide(color: Color(0xFF1F2B44), width: 1.2),
        ),
        dividerTheme: const DividerThemeData(
          color: Color(0xFF1F2B44),
          thickness: 1.2,
          space: 24,
        ),
        textTheme: const TextTheme(
          titleLarge: TextStyle(color: Colors.white, fontWeight: FontWeight.black, fontSize: 22, letterSpacing: 0.5),
          titleMedium: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16),
          bodyMedium: TextStyle(color: Color(0xFF94A3B8), fontSize: 13, height: 1.4),
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: const Color(0xFF0C1322),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: const BorderSide(color: Color(0xFF1F2B44)),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: const BorderSide(color: Color(0xFFCA8A04), width: 1.5),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(8),
            borderSide: const BorderSide(color: Color(0xFF1F2B44)),
          ),
        ),
      ),
      home: const DashboardScreen(),
    );
  }
}
