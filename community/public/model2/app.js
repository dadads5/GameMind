document.addEventListener('DOMContentLoaded', () => {
    // DOM 元素提前缓存
    const elements = {
        charTabsContainer: document.getElementById('character-tabs'),
        charDetailContainer: document.getElementById('character-detail'),
        cardGrid: document.getElementById('card-grid'),
        cardFilter: document.getElementById('card-filter'),
        relicList: document.getElementById('relic-list'),
        potionList: document.getElementById('potion-list'),
        postGrid: document.getElementById('post-grid'),
        postModal: document.getElementById('post-modal'),
        modalTitle: document.getElementById('modal-title'),
        modalAuthor: document.getElementById('modal-author'),
        modalDate: document.getElementById('modal-date'),
        modalCategory: document.getElementById('modal-category'),
        modalContent: document.getElementById('modal-content'),
        modalClose: document.getElementById('modal-close')
    };

    let activeCharId = GAME_DATA.characters[0].id;

    // 事件委托处理
    function setupEventDelegation() {
        // 角色标签点击
        elements.charTabsContainer.addEventListener('click', (e) => {
            const tab = e.target.closest('button[data-char-id]');
            if (tab) {
                switchCharacter(tab.dataset.charId);
            }
        });

        // 卡牌过滤器点击
        elements.cardFilter.addEventListener('click', (e) => {
            const btn = e.target.closest('button[data-filter]');
            if (btn) {
                renderCards(btn.dataset.filter);
            }
        });

        // 帖子点击
        elements.postGrid.addEventListener('click', (e) => {
            const post = e.target.closest('.post-card[data-post-id]');
            if (post) {
                openModal(parseInt(post.dataset.postId));
            }
        });

        // 弹窗关闭按钮
        const modalClose = elements.postModal.querySelector('#modal-close');
        if (modalClose) {
            modalClose.addEventListener('click', closeModal);
        }

        // 弹窗背景点击
        elements.postModal.addEventListener('click', (e) => {
            if (e.target === elements.postModal) {
                closeModal();
            }
        });
    }

    // 创建元素工具函数
    function createElement(tag, className = '', innerHTML = '') {
        const el = document.createElement(tag);
        if (className) el.className = className;
        if (innerHTML) el.innerHTML = innerHTML;
        return el;
    }

    // 渲染角色选项卡（使用 DocumentFragment）
    function renderTabs() {
        const fragment = document.createDocumentFragment();

        GAME_DATA.characters.forEach(char => {
            const btn = createElement('button', 'flex flex-col items-center p-4 rounded-xl border-2 border-slate-800 bg-slate-900/50 hover:border-slate-600 transition-all group');
            btn.dataset.charId = char.id;
            btn.id = `tab-${char.id}`;
            btn.innerHTML = `
                <i class="fas ${char.icon} text-2xl mb-2 group-hover:scale-110 transition-transform"></i>
                <span class="font-bold">${char.name}</span>
            `;
            fragment.appendChild(btn);
        });

        elements.charTabsContainer.innerHTML = '';
        elements.charTabsContainer.appendChild(fragment);
    }

    // 切换角色逻辑
    function switchCharacter(id) {
        document.querySelectorAll('#character-tabs button').forEach(btn => btn.classList.remove('char-tab-active'));
        const tabBtn = document.getElementById(`tab-${id}`);
        if (tabBtn) {
            tabBtn.classList.add('char-tab-active');
        }

        elements.charDetailContainer.classList.remove('fade-in-up');
        setTimeout(() => {
            showCharacterDetail(id);
            elements.charDetailContainer.classList.add('fade-in-up');
        }, 200);

        activeCharId = id;
    }

    // 显示角色详情
    function showCharacterDetail(id) {
        const char = GAME_DATA.characters.find(c => c.id === id);
        if (!char) return;

        let bgGradient = 'from-blue-600 to-cyan-600';
        if (char.id === 'ironclad') bgGradient = 'from-red-600 to-orange-600';
        else if (char.id === 'silent') bgGradient = 'from-green-600 to-emerald-600';
        else if (char.id === 'necrobinder') bgGradient = 'from-purple-600 to-pink-600';
        else if (char.id === 'primarch') bgGradient = 'from-amber-400 to-yellow-600';

        elements.charDetailContainer.innerHTML = `
            <div class="grid md:grid-cols-2 gap-10 items-center">
                <div>
                    <div class="flex items-center gap-3 mb-4">
                        <span class="px-3 py-1 bg-slate-800 rounded-full text-xs font-bold uppercase tracking-wider text-slate-400">
                            ${char.title}
                        </span>
                        <span class="text-sm font-medium ${char.color}">难度: ${char.difficulty}</span>
                    </div>
                    <h2 class="text-4xl font-black mb-6 ${char.color}">${char.name}</h2>
                    <p class="text-slate-300 leading-relaxed mb-8 text-lg">${char.description}</p>
                    
                    <div class="space-y-6">
                        <div>
                            <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">推荐流派</h4>
                            <div class="flex flex-wrap gap-2 mb-6">
                                ${char.recommendedDecks.map(d => `
                                    <span class="px-3 py-1 rounded-md bg-slate-800 border border-slate-700 text-sm text-slate-300">
                                        ${d}
                                    </span>
                                `).join('')}
                            </div>
                            <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">核心机制</h4>
                            <div class="flex flex-wrap gap-2">
                                ${char.mechanics.map(m => `
                                    <span class="px-3 py-1 rounded-md bg-slate-800 border border-slate-700 text-sm">
                                        ${m}
                                    </span>
                                `).join('')}
                            </div>
                        </div>
                        <div>
                            <h4 class="text-sm font-bold text-slate-500 uppercase mb-3">进阶攻略</h4>
                            <p class="text-slate-400 text-sm leading-relaxed bg-slate-950 p-4 rounded-lg border-l-4 ${char.borderColor}">
                                ${char.strategy}
                            </p>
                        </div>
                    </div>
                </div>
                <div class="relative group">
                    <div class="absolute -inset-1 bg-gradient-to-r ${bgGradient} rounded-2xl blur opacity-25 group-hover:opacity-50 transition duration-1000"></div>
                    <div class="relative bg-slate-950 rounded-2xl p-8 border border-slate-800 text-center">
                        <i class="fas ${char.icon} text-9xl ${char.color} opacity-20 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"></i>
                        <div class="relative z-10 py-20">
                            <p class="text-slate-500 italic">" 准备好迎接塔楼的挑战了吗？ "</p>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const tabBtn = document.getElementById(`tab-${id}`);
        if (tabBtn) {
            tabBtn.classList.add('char-tab-active');
        }
        elements.charDetailContainer.classList.add('fade-in-up');
    }

    // 渲染过滤器
    function renderFilters() {
        const fragment = document.createDocumentFragment();
        const filters = ['all', ...GAME_DATA.characters.map(c => c.id)];

        filters.forEach(f => {
            const char = GAME_DATA.characters.find(c => c.id === f);
            const label = f === 'all' ? '全部卡牌' : char.name;
            const btn = createElement('button', 'px-4 py-2 rounded-full text-sm font-medium border border-slate-700 hover:border-red-500 hover:text-red-500 transition-all');
            btn.dataset.filter = f;
            btn.textContent = label;
            fragment.appendChild(btn);
        });

        elements.cardFilter.innerHTML = '';
        elements.cardFilter.appendChild(fragment);
    }

    function getRarityClass(rarity) {
        return `rarity-${rarity}`;
    }

    // 渲染卡牌
    function renderCards(filter) {
        const filteredCards = filter === 'all' 
            ? GAME_DATA.cards 
            : GAME_DATA.cards.filter(c => c.charId === filter);

        const fragment = document.createDocumentFragment();

        filteredCards.forEach(card => {
            const char = GAME_DATA.characters.find(c => c.id === card.charId);
            const cardEl = createElement('div', 'game-card bg-slate-900 rounded-xl p-6 border border-slate-800 flex flex-col h-full');
            cardEl.innerHTML = `
                <div class="flex justify-between items-start mb-4">
                    <span class="text-xs font-bold px-2 py-0.5 rounded bg-slate-800 ${char.color}">${char.name}</span>
                    <div class="energy-icon">${card.cost}</div>
                </div>
                <h3 class="text-xl font-bold mb-2 ${getRarityClass(card.rarity)}">${card.name}</h3>
                <div class="text-xs text-slate-500 uppercase mb-3">${card.type}</div>
                <p class="text-slate-300 text-sm flex-grow mb-4 leading-relaxed">${card.description}</p>
                <div class="mt-auto pt-4 border-t border-slate-800">
                    <div class="text-[10px] text-slate-500 uppercase font-bold mb-1">推荐 Combo</div>
                    <p class="text-xs text-slate-400 italic">${card.combo}</p>
                </div>
            `;
            fragment.appendChild(cardEl);
        });

        elements.cardGrid.innerHTML = '';
        elements.cardGrid.appendChild(fragment);
    }

    // 渲染遗物
    function renderRelics() {
        const fragment = document.createDocumentFragment();

        GAME_DATA.relics.forEach(relic => {
            const char = relic.charId === 'all' ? null : GAME_DATA.characters.find(c => c.id === relic.charId);
            const relicEl = createElement('div', 'relic-card p-4 rounded-xl flex items-center gap-4');
            relicEl.innerHTML = `
                <div class="w-12 h-12 bg-slate-950 rounded-lg flex items-center justify-center text-2xl text-red-500 border border-slate-800">
                    <i class="fas ${relic.icon}"></i>
                </div>
                <div class="flex-grow">
                    <div class="flex items-center gap-2 mb-1">
                        <h4 class="font-bold text-slate-100">${relic.name}</h4>
                        <span class="text-[10px] px-1.5 py-0.5 rounded bg-slate-800 text-slate-500 uppercase">${relic.rarity}</span>
                        ${char ? `<span class="text-[10px] ${char.color}">${char.name}</span>` : ''}
                    </div>
                    <p class="text-xs text-slate-400">${relic.description}</p>
                </div>
            `;
            fragment.appendChild(relicEl);
        });

        elements.relicList.innerHTML = '';
        elements.relicList.appendChild(fragment);
    }

    // 渲染药水
    function renderPotions() {
        const fragment = document.createDocumentFragment();

        GAME_DATA.potions.forEach(potion => {
            const potionEl = createElement('div', 'potion-item p-4 rounded-xl flex items-center gap-4 bg-slate-900/50');
            potionEl.innerHTML = `
                <div class="text-2xl text-purple-500">
                    <i class="fas ${potion.icon}"></i>
                </div>
                <div>
                    <h4 class="font-bold text-slate-100 text-sm">${potion.name}</h4>
                    <p class="text-xs text-slate-400">${potion.description}</p>
                </div>
            `;
            fragment.appendChild(potionEl);
        });

        elements.potionList.innerHTML = '';
        elements.potionList.appendChild(fragment);
    }

    // 渲染帖子
    function renderPosts() {
        const fragment = document.createDocumentFragment();

        GAME_DATA.posts.forEach(post => {
            const postEl = createElement('div', 'post-card rounded-2xl overflow-hidden flex flex-col h-full cursor-pointer');
            postEl.dataset.postId = post.id;
            postEl.innerHTML = `
                <div class="h-40 bg-slate-800 relative overflow-hidden">
                    <div class="absolute inset-0 flex items-center justify-center text-6xl text-slate-700 opacity-20">
                        <i class="fas ${post.icon}"></i>
                    </div>
                    <div class="absolute bottom-4 left-4">
                        <span class="px-2 py-1 bg-red-600 text-white text-[10px] font-bold rounded uppercase">${post.category}</span>
                    </div>
                </div>
                <div class="p-6 flex-grow flex flex-col">
                    <h3 class="text-lg font-bold mb-3 hover:text-red-500 transition-colors line-clamp-2">${post.title}</h3>
                    <p class="text-slate-400 text-sm mb-4 line-clamp-3 flex-grow">${post.summary}</p>
                    <div class="flex items-center justify-between mt-auto pt-4 border-t border-slate-800">
                        <div class="flex items-center gap-2">
                            <div class="w-6 h-6 bg-slate-700 rounded-full flex items-center justify-center text-[10px]">
                                <i class="fas fa-user"></i>
                            </div>
                            <span class="text-xs text-slate-500">${post.author}</span>
                        </div>
                        <span class="text-[10px] text-slate-600">${post.date}</span>
                    </div>
                </div>
            `;
            fragment.appendChild(postEl);
        });

        elements.postGrid.innerHTML = '';
        elements.postGrid.appendChild(fragment);
    }

    // 弹窗逻辑
    function openModal(id) {
        const post = GAME_DATA.posts.find(p => p.id === id);
        if (!post) return;

        elements.modalTitle.innerText = post.title;
        elements.modalAuthor.innerText = post.author;
        elements.modalDate.innerText = post.date;
        elements.modalCategory.innerText = post.category;

        let specificContent = '';
        if (id === 1) {
            specificContent = `
                <div class="space-y-6">
                    <p class="text-xl leading-relaxed">
                        新手开局优先选择 <strong class="text-red-500">战士</strong> 上手，卡组简单、生存能力强，更容易熟悉玩法。游戏核心是 <strong class="text-red-400">精简卡组、把控血线、合理选遗物</strong>，切忌盲目拿牌。
                    </p>
                    <div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700">
                        <h4 class="text-lg font-bold mb-3 text-blue-400 flex items-center gap-2">
                            <i class="fas fa-shield-alt"></i> 战斗与卡组建议
                        </h4>
                        <p class="mb-4">战斗初期以稳防御为主，别一味堆攻击，用格挡稳住血量，避免前期掉血过多。卡牌优先挑选低费实用牌，多余杂牌尽量在商店处删除，卡组越精简，抽牌稳定性越高。</p>
                        <p>遗物优先选取加费用、减伤害、提升容错的类型，大幅提升通关概率。</p>
                    </div>
                    <div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700">
                        <h4 class="text-lg font-bold mb-3 text-orange-400 flex items-center gap-2">
                            <i class="fas fa-map-marked-alt"></i> 路线与探索规划
                        </h4>
                        <p class="mb-4">每层探索优先走问号与篝火，篝火优先回血、删牌，少点升级。遇到精英怪量力而行，前期实力不足可绕开。打 BOSS 前务必补满状态，规划好出牌顺序。</p>
                    </div>
                    <div class="bg-slate-950 p-6 rounded-2xl border border-red-900/30 italic text-slate-400">
                        循序渐进熟悉各卡牌联动与流派思路，先尝试基础力量流、防御流，熟练后再探索进阶体系。多复盘失败对局，慢慢掌握节奏，就能稳步通关塔楼。
                    </div>
                </div>
            `;
        } else if (id === 2) {
            specificContent = `
                <div class="space-y-6">
                    <p class="text-xl leading-relaxed">
                        <strong class="text-purple-500">死灵术师（Necrobinder）</strong> 是《杀戮尖塔 2》当前版本综合强度天花板，凭借独特机制与高上限构筑稳居 <strong class="text-red-500">T0 梯队</strong>。
                    </p>
                    <div class="grid md:grid-cols-3 gap-4">
                        <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30">
                            <h5 class="font-bold text-purple-400 mb-2">灾厄 (Doom)</h5>
                            <p class="text-xs text-slate-400">无视格挡直接斩杀，叠满即秒杀，上限远超中毒体系。</p>
                        </div>
                        <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30">
                            <h5 class="font-bold text-purple-400 mb-2">奥斯提 (Osty)</h5>
                            <p class="text-xs text-slate-400">召唤物替本体承伤，解决自身低血量短板，攻防一体。</p>
                        </div>
                        <div class="bg-purple-900/20 p-4 rounded-xl border border-purple-500/30">
                            <h5 class="font-bold text-purple-400 mb-2">灵魂系统</h5>
                            <p class="text-xs text-slate-400">提供 0 费过牌与减费，卡组运转极快，易形成无限循环。</p>
                        </div>
                    </div>
                    <div class="bg-slate-800/50 p-6 rounded-2xl border border-slate-700">
                        <h4 class="text-lg font-bold mb-4 text-slate-100">流派选择与核心卡</h4>
                        <ul class="space-y-3 text-sm">
                            <li class="flex items-start gap-2">
                                <i class="fas fa-bolt text-yellow-500 mt-1"></i>
                                <span><strong>灾厄流：</strong> 启动快、斩杀稳，适合应对高甲敌人。</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <i class="fas fa-ghost text-blue-400 mt-1"></i>
                                <span><strong>召唤流：</strong> 容错高、后期强势，通过海量召唤物淹没对手。</span>
                            </li>
                            <li class="flex items-start gap-2">
                                <i class="fas fa-magic text-purple-400 mt-1"></i>
                                <span><strong>灵魂流：</strong> 上限极高，适配多种遗物，追求极致的单回合爆发。</span>
                            </li>
                        </ul>
                        <div class="mt-6 p-4 bg-purple-950/30 rounded-lg border-l-4 border-purple-500">
                            <p><strong>核心卡推荐：【挽歌】</strong>—— 兼具过牌与强化召唤，成型后几乎无解。</p>
                        </div>
                    </div>
                    <p class="text-slate-300 italic">
                        虽前期本体脆弱、怕暴毙，但成型后输出无解、生存稳定、运转丝滑，适配所有难度，是当前版本最值得练的最强角色。
                    </p>
                </div>
            `;
        } else if (id === 3) {
            specificContent = `
                <div class="space-y-8">
                    <p class="mb-6">基于上千场对局数据，我们总结出了《杀戮尖塔2》当前版本的全遗物强度排名。本排名旨在为玩家提供选取参考，具体优先级仍需根据你的当前卡组构筑决定。</p>
                    <section>
                        <h4 class="text-2xl font-black text-red-500 mb-4 border-b border-red-900/50 pb-2">SS 级（超模遗物）</h4>
                        <div class="grid gap-4">
                            <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500">
                                <p><strong class="text-red-400">枯木树枝：</strong>打出耗尽牌时，随机将一张牌加入手牌。无限流、烧牌流核心，泛用性极强。</p>
                            </div>
                            <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500">
                                <p><strong class="text-red-400">冰淇淋：</strong>每回合未使用的费用可以保留至下一回合。全职业通用顶级续航，彻底解决卡费问题。</p>
                            </div>
                            <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500">
                                <p><strong class="text-red-400">木乃伊之手：</strong>使用一张能力牌后，本回合所有手牌费用 - 1。能力流、无限流核心启动件。</p>
                            </div>
                            <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500">
                                <p><strong class="text-red-400">准备背包：</strong>每局战斗首回合额外抽 2 张牌。稳定起手，全流派通用。</p>
                            </div>
                            <div class="bg-slate-800/50 p-4 rounded-xl border-l-4 border-red-500">
                                <p><strong class="text-red-400">赌徒筹码：</strong>每局战斗开局可免费重抽一次手牌。规避起手崩盘，容错拉满。</p>
                            </div>
                        </div>
                    </section>
                    <section>
                        <h4 class="text-2xl font-black text-orange-500 mb-4 border-b border-orange-900/50 pb-2">S 级（强度顶尖）</h4>
                        <div class="grid gap-3">
                            <p><strong>钨钢棒：</strong>受到的所有伤害永久减少 1 点。最强常驻减伤，全程稳血线。</p>
                            <p><strong>纸青蛙：</strong>施加的易伤效果额外提升 50% 伤害。爆发流通用核心。</p>
                            <p><strong>灯笼：</strong>每局战斗首回合获得 1 点额外费用。前期节奏神器，加快启动。</p>
                            <p><strong>卡戎之灰：</strong>每打出一张耗尽牌，对所有敌人造成 3 点伤害。烧牌流清场核心。</p>
                            <p><strong>符文圆顶：</strong>免疫虚弱、易伤、中毒效果。对抗负面体系的防御神物。</p>
                            <p><strong>灵体外质：</strong>永久增加 1 点最大费用，几乎无负面。顶级 BOSS 遗物。</p>
                        </div>
                    </section>
                    <section>
                        <h4 class="text-xl font-bold text-yellow-500 mb-4">A 级（实力强劲）</h4>
                        <p class="text-slate-400 text-sm leading-relaxed">装饰折扇、蜥蜴尾巴、怀表、毒蛋、冠军腰带、冻结之眼。这些遗物适配多数主流卡组，单卡收益非常可观。</p>
                    </section>
                    <section class="bg-slate-950 p-6 rounded-2xl border border-slate-800">
                        <h4 class="text-lg font-bold text-slate-100 mb-4 flex items-center gap-2">
                            <i class="fas fa-bullseye text-red-500"></i> 分角色简易选取参考
                        </h4>
                        <div class="grid md:grid-cols-2 gap-6 text-sm">
                            <div><strong class="text-red-500">战士：</strong> 优先钨钢棒、灯笼、纸青蛙、熔岩蛋。</div>
                            <div><strong class="text-green-500">静默猎手：</strong> 优先毒蛋、纸青蛙、苦无。</div>
                            <div><strong class="text-blue-500">机器人：</strong> 优先冰淇淋、木乃伊之手、枯木树枝。</div>
                            <div><strong class="text-amber-500">观者：</strong> 优先怀表、冻结之眼、百年拼图。</div>
                        </div>
                    </section>
                </div>
            `;
        } else {
            specificContent = `<p>${post.content}</p><p>更多详细内容正在整理中...</p>`;
        }

        elements.modalContent.innerHTML = specificContent;
        elements.postModal.classList.remove('hidden');
        elements.postModal.classList.add('flex');
        document.body.style.overflow = 'hidden';
    }

    function closeModal() {
        elements.postModal.classList.add('hidden');
        elements.postModal.classList.remove('flex');
        document.body.style.overflow = 'auto';
    }

    // 初始化
    function init() {
        setupEventDelegation();
        renderTabs();
        showCharacterDetail(activeCharId);
        renderFilters();
        renderCards('all');
        renderRelics();
        renderPotions();
        renderPosts();
    }

    init();
});
