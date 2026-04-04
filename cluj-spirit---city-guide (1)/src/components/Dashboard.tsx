import React from 'react';
import { motion } from 'motion/react';
import { Cloud, Sun, Map as MapIcon, Plus, Camera, Music, GraduationCap, Leaf, ChevronRight } from 'lucide-react';
import { CATEGORIES } from '../constants';

const iconMap: Record<string, any> = {
  Camera,
  Music,
  GraduationCap,
  Leaf,
};

export const Dashboard: React.FC = () => {
  return (
    <div className="pb-32 px-6 pt-12 space-y-8 max-w-md mx-auto">
      {/* Header with Points */}
      <div className="flex justify-end">
        <div className="bg-black/40 backdrop-blur-md border border-cluj-accent/30 rounded-full px-4 py-2 flex items-center gap-2 accent-glow">
          <div className="w-5 h-5 bg-cluj-gold rounded-full flex items-center justify-center text-[10px] text-black font-bold">★</div>
          <span className="text-sm font-medium text-white">Puncte: <span className="font-bold">18745</span></span>
        </div>
      </div>

      {/* Hero Blob Image */}
      <div className="relative h-64 w-full flex items-center justify-center">
        <motion.div
          animate={{
            borderRadius: ["40% 60% 70% 30% / 40% 50% 60% 50%", "60% 40% 30% 70% / 50% 60% 40% 60%", "40% 60% 70% 30% / 40% 50% 60% 50%"],
          }}
          transition={{
            duration: 8,
            repeat: Infinity,
            ease: "easeInOut"
          }}
          className="w-full h-full overflow-hidden border-4 border-cluj-accent/20 accent-glow"
        >
          <img
            src="https://picsum.photos/seed/cluj-center/800/600"
            alt="Cluj-Napoca"
            className="w-full h-full object-cover"
            referrerPolicy="no-referrer"
          />
        </motion.div>
      </div>

      {/* Weather and Map Grid */}
      <div className="grid grid-cols-2 gap-4">
        <div className="glass-card p-6 flex flex-col justify-between h-48 relative overflow-hidden group">
          <div className="absolute -right-4 -top-4 opacity-10 group-hover:opacity-20 transition-opacity">
            <Cloud size={120} />
          </div>
          <div>
            <h4 className="text-lg font-display font-bold text-white leading-tight">Vremea în Cluj-Napoca</h4>
          </div>
          <div className="flex items-center gap-4">
            <div className="relative">
              <Sun className="text-cluj-accent" size={32} />
              <Cloud className="absolute -bottom-1 -right-1 text-white" size={20} />
            </div>
            <div>
              <div className="text-3xl font-bold">21°C</div>
              <div className="text-xs text-gray-400">Parțial Noros</div>
            </div>
          </div>
          <button className="w-full py-2 bg-cluj-accent/20 border border-cluj-accent/30 rounded-xl text-xs font-bold text-cluj-accent hover:bg-cluj-accent/30 transition-all">
            Actualizează
          </button>
        </div>

        <div className="glass-card p-6 flex flex-col justify-between h-48 bg-gradient-to-br from-white/5 to-cluj-accent/10 group cursor-pointer">
          <div className="w-12 h-12 bg-cluj-accent/20 rounded-2xl flex items-center justify-center text-cluj-accent group-hover:scale-110 transition-transform">
            <MapIcon size={28} />
          </div>
          <div>
            <h4 className="text-lg font-display font-bold text-white leading-tight">Harta Interactivă Cluj-Napoca</h4>
          </div>
        </div>
      </div>

      {/* Discover Section */}
      <div className="space-y-4">
        <h3 className="text-xl font-display font-bold text-white">Descoperă Orașul</h3>
        <div className="flex gap-4 overflow-x-auto pb-4 no-scrollbar">
          {CATEGORIES.map((cat) => {
            const Icon = iconMap[cat.icon || 'Camera'];
            return (
              <div key={cat.id} className="min-w-[280px] glass-card overflow-hidden group cursor-pointer">
                <div className="h-32 relative overflow-hidden">
                  <img src={cat.imageUrl} alt={cat.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" referrerPolicy="no-referrer" />
                  <div className="absolute inset-0 bg-black/40" />
                  <div className="absolute top-4 left-4 flex items-center gap-3">
                    <div className="w-10 h-10 bg-cluj-accent/80 rounded-xl flex items-center justify-center text-white">
                      <Icon size={20} />
                    </div>
                    <span className="font-bold text-white">{cat.title}</span>
                  </div>
                </div>
                <div className="p-4">
                  <p className="text-xs text-gray-400 line-clamp-2 leading-relaxed">
                    {cat.description}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Floating Action Button */}
      <button className="fixed bottom-28 right-8 w-14 h-14 bg-cluj-accent rounded-2xl flex items-center justify-center text-white shadow-lg accent-glow hover:scale-110 transition-transform z-40">
        <Plus size={32} />
      </button>
    </div>
  );
};
