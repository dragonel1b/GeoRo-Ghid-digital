import React, { useState } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { BottomNav } from './components/BottomNav';
import { Dashboard } from './components/Dashboard';
import { EventsList } from './components/EventsList';
import { BentoGuide } from './components/BentoGuide';

export default function App() {
  const [activeTab, setActiveTab] = useState('home');

  const renderScreen = () => {
    switch (activeTab) {
      case 'home':
        return <Dashboard key="dashboard" />;
      case 'events':
        return <EventsList key="events" />;
      case 'explore':
        return <BentoGuide key="explore" />;
      case 'profile':
        return (
          <div className="flex items-center justify-center h-screen text-gray-500 font-display font-bold text-2xl">
            Profile Screen
          </div>
        );
      default:
        return <Dashboard key="dashboard" />;
    }
  };

  return (
    <div className="min-h-screen bg-cluj-dark selection:bg-cluj-accent selection:text-white overflow-x-hidden">
      {/* Background Gradients */}
      <div className="fixed inset-0 pointer-events-none z-0">
        <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-cluj-green/10 via-transparent to-cluj-blue/10" />
        <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-cluj-green/20 blur-[120px] rounded-full" />
        <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-cluj-blue/20 blur-[120px] rounded-full" />
      </div>

      <main className="relative z-10">
        <AnimatePresence mode="wait">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ duration: 0.4, ease: "easeOut" }}
          >
            {renderScreen()}
          </motion.div>
        </AnimatePresence>
      </main>

      <BottomNav activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
}
