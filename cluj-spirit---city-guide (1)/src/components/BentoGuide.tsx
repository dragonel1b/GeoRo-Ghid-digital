import React from 'react';
import { motion } from 'motion/react';
import { GraduationCap, Music, Camera, Leaf, ChevronRight, Plus, Info } from 'lucide-react';

export const BentoGuide: React.FC = () => {
  const items = [
    { title: 'Viața Universitară', icon: GraduationCap, color: 'bg-cluj-accent', size: 'col-span-2 row-span-1' },
    { title: 'Atracții Turistice', icon: Camera, color: 'bg-cluj-accent', size: 'col-span-1 row-span-1' },
    { title: 'Cultură și Festivaluri', icon: Music, color: 'bg-cluj-accent', size: 'col-span-1 row-span-1' },
    { title: 'Natură și Parcuri', icon: Leaf, color: 'bg-cluj-accent', size: 'col-span-2 row-span-1' },
  ];

  return (
    <div className="pb-32 px-6 pt-12 space-y-8 max-w-md mx-auto">
      <div className="flex items-center justify-between">
        <button className="p-2 glass-card rounded-xl">
          <ChevronRight size={24} className="rotate-180" />
        </button>
        <h2 className="text-xl font-display font-bold text-white">Bento Interactive Guide</h2>
        <div className="w-10" />
      </div>

      <div className="grid grid-cols-2 gap-4 auto-rows-[160px]">
        {items.map((item, idx) => (
          <motion.div
            key={idx}
            whileHover={{ scale: 1.02 }}
            className={`relative glass-card overflow-hidden group cursor-pointer ${item.size}`}
          >
            <div className="absolute inset-0 bg-black/60 z-10" />
            <img 
              src={`https://picsum.photos/seed/bento-${idx}/600/400`} 
              alt={item.title} 
              className="absolute inset-0 w-full h-full object-cover group-hover:scale-110 transition-transform duration-700" 
              referrerPolicy="no-referrer"
            />
            
            <div className="relative z-20 h-full p-6 flex flex-col items-center justify-center text-center gap-4">
              <div className="w-16 h-16 bg-cluj-accent/90 rounded-2xl flex items-center justify-center text-white shadow-lg accent-glow group-hover:scale-110 transition-transform">
                <item.icon size={32} />
              </div>
              <h3 className="text-lg font-display font-bold text-white tracking-tight leading-tight">
                {item.title}
              </h3>
            </div>

            <div className="absolute bottom-4 right-4 z-30 flex gap-2">
              <button className="w-8 h-8 bg-black/40 backdrop-blur-md border border-white/20 rounded-full flex items-center justify-center text-white hover:bg-cluj-accent transition-colors">
                <ChevronRight size={16} />
              </button>
            </div>
          </motion.div>
        ))}
      </div>

      {/* Floating Action Buttons */}
      <div className="fixed bottom-28 right-8 flex flex-col gap-4 z-40">
        <button className="w-12 h-12 bg-cluj-accent/20 backdrop-blur-md border border-cluj-accent/30 rounded-full flex items-center justify-center text-cluj-accent hover:bg-cluj-accent hover:text-white transition-all">
          <Info size={24} />
        </button>
        <button className="w-14 h-14 bg-cluj-accent rounded-2xl flex items-center justify-center text-white shadow-lg accent-glow hover:scale-110 transition-transform">
          <Plus size={32} />
        </button>
      </div>
    </div>
  );
};
