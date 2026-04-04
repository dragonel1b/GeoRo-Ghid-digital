import React from 'react';
import { Search } from 'lucide-react';

export const SearchBar: React.FC = () => {
  return (
    <div className="relative w-full max-w-md mx-auto px-6 pt-8 pb-4">
      <div className="relative group">
        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
          <Search size={20} className="text-gray-500 group-focus-within:text-cluj-accent transition-colors" />
        </div>
        <input
          type="text"
          placeholder="Caută spiritul Clujului..."
          className="block w-full pl-12 pr-4 py-4 bg-white/5 border border-white/10 rounded-2xl text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-cluj-accent/50 focus:border-cluj-accent/50 transition-all backdrop-blur-md"
        />
      </div>
    </div>
  );
};
