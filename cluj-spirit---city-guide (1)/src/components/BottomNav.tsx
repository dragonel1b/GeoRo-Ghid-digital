import React from 'react';
import { Home, Compass, Calendar, User } from 'lucide-react';
import { motion } from 'motion/react';

interface BottomNavProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

export const BottomNav: React.FC<BottomNavProps> = ({ activeTab, setActiveTab }) => {
  const tabs = [
    { id: 'home', icon: Home, label: 'Home' },
    { id: 'explore', icon: Compass, label: 'Explore' },
    { id: 'events', icon: Calendar, label: 'Events' },
    { id: 'profile', icon: User, label: 'Profile' },
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 p-6 z-50">
      <div className="max-w-md mx-auto glass-card flex items-center justify-around py-4 px-2 shadow-2xl">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className="relative p-3 group"
          >
            {activeTab === tab.id && (
              <motion.div
                layoutId="activeTab"
                className="absolute inset-0 bg-cluj-accent/20 rounded-2xl"
                transition={{ type: 'spring', bounce: 0.2, duration: 0.6 }}
              />
            )}
            <tab.icon
              size={24}
              className={`relative z-10 transition-colors duration-300 ${
                activeTab === tab.id ? 'text-cluj-accent' : 'text-gray-500 group-hover:text-gray-300'
              }`}
            />
          </button>
        ))}
      </div>
    </div>
  );
};
