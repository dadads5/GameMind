const API_BASE_URL = '/api';

document.addEventListener('DOMContentLoaded', function() {
    displayDefaultStats();
    displayDefaultCharacters();
});

function displayDefaultStats() {
    const defaultStats = { posts: 5678, characters: 67, todayPosts: 50, totalViews: 123456 };
    
    fetch(`${API_BASE_URL}/posts/game/statistics?gameIds=1,2,3,4,5,6`).then(res => res.json()).then(data => {
        if (data.totalPosts !== undefined) {
            defaultStats.posts = data.totalPosts;
            defaultStats.todayPosts = data.todayPosts || 0;
            defaultStats.totalViews = data.totalViews || 0;
        }
        renderStats(defaultStats);
    }).catch(() => renderStats(defaultStats));
    
    fetch(`${API_BASE_URL}/characters/count`).then(res => res.json()).then(data => {
        if (data.success) {
            document.querySelector('.stat-card:nth-child(2) .stat-value').textContent = data.data;
        }
    }).catch(() => {});
    
    renderStats(defaultStats);
}

function renderStats(stats) {
    document.getElementById('stats-grid').innerHTML = `
        <div class="stat-card">
            <div class="stat-icon">📝</div>
            <div class="stat-value">${stats.posts.toLocaleString()}</div>
            <div class="stat-label">论坛帖子</div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🎭</div>
            <div class="stat-value">${stats.characters}</div>
            <div class="stat-label">收录角色</div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">🔥</div>
            <div class="stat-value">${stats.todayPosts}</div>
            <div class="stat-label">今日新增</div>
        </div>
        <div class="stat-card">
            <div class="stat-icon">👁️</div>
            <div class="stat-value">${(stats.totalViews / 10000).toFixed(1)}万</div>
            <div class="stat-label">总浏览量</div>
        </div>
    `;
}

function displayDefaultCharacters() {
    const characters = [
        { name: '娜维娅', subtitle: '枫丹', element: '岩', icon: 'NAVIYA' },
        { name: '雷电将军', subtitle: '稻妻', element: '雷', icon: 'RAIDEN' },
        { name: '芙宁娜', subtitle: '枫丹', element: '水', icon: 'FURINA' },
        { name: '艾尔海森', subtitle: '须弥', element: '草', icon: 'ALHAITHAM' },
        { name: '胡桃', subtitle: '璃月', element: '火', icon: 'HU TAO' },
        { name: '万叶', subtitle: '稻妻', element: '风', icon: 'KAZUHA' }
    ];
    
    fetch(`${API_BASE_URL}/characters/hot?limit=6`).then(res => res.json()).then(data => {
        if (data.success && data.data && data.data.length > 0) {
            renderCharacters(data.data);
        }
    }).catch(() => {});
    
    renderCharacters(characters);
}

function getElementColor(element) {
    const colors = {
        '火': { bg: 'rgba(255,107,107,0.2)', text: '#ff6b6b', icon: '🔥' },
        '水': { bg: 'rgba(0,206,201,0.2)', text: '#00cec9', icon: '💧' },
        '草': { bg: 'rgba(0,184,148,0.2)', text: '#00b894', icon: '🌿' },
        '雷': { bg: 'rgba(162,155,254,0.2)', text: '#a29bfe', icon: '⚡' },
        '冰': { bg: 'rgba(116,185,255,0.2)', text: '#74b9ff', icon: '❄️' },
        '岩': { bg: 'rgba(253,203,110,0.2)', text: '#fdcb6e', icon: '🪨' },
        '风': { bg: 'rgba(99,110,114,0.2)', text: '#636e72', icon: '💨' }
    };
    return colors[element] || colors['风'];
}

function renderCharacters(characters) {
    document.getElementById('characters-grid').innerHTML = characters.map(char => {
        const color = getElementColor(char.element);
        return `
            <div class="character-card">
                <div class="character-icon-wrapper" style="background: ${color.bg}; border: 2px solid ${color.text};">
                    ${color.icon}
                </div>
                <div class="character-element" style="color: ${color.text};">${char.element}</div>
                <div class="character-info">
                    <div class="character-name">${char.name}</div>
                    <div class="character-region">${char.subtitle}</div>
                    <div class="character-stats">
                        <div class="stat-item"><div class="stat-num">${Math.floor(Math.random() * 3000 + 5000)}</div><div class="stat-name">战力</div></div>
                        <div class="stat-item"><div class="stat-num">${Math.floor(Math.random() * 50 + 80)}</div><div class="stat-name">人气</div></div>
                        <div class="stat-item"><div class="stat-num">${Math.floor(Math.random() * 200 + 500)}</div><div class="stat-name">攻略</div></div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function navigateTo(url) {
    window.location.href = url;
}