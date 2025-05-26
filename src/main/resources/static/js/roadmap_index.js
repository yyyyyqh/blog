// 等待 DOM 内容加载完成
document.addEventListener("DOMContentLoaded", function() {
    // 获取页面上的重要元素
    const pdfGridContainer = document.getElementById('pdf-grid-container'); // 网格容器
    const pdfOptionsContainer = document.getElementById('pdf-options-container'); // 选项按钮容器
    const nativeReadBtn = document.getElementById('native-read-btn'); // 原生阅读按钮
    const offlineReadBtn = document.getElementById('offline-read-btn'); // 离线阅读按钮
    const pdfViewerContainer = document.getElementById('pdf-viewer-container'); // PDF 查看器容器
    const pdfIframe = document.getElementById('pdf-iframe'); // 内嵌的 iframe
    const pdfDownloadLink = document.getElementById('pdf-download-link'); // iframe 备用链接

    let currentPdfUrl = null; // 用于存储当前选中 PDF 的 URL

    // 页面加载时隐藏选项和查看器
    pdfOptionsContainer.style.display = 'none';
    pdfViewerContainer.style.display = 'none';

    // **为网格容器添加点击事件监听器 (使用事件委托)**
    // 点击网格内的任何地方，都会检查是否点击了 .pdf-card
    pdfGridContainer.addEventListener('click', function(event) {
        // closest('.pdf-card') 查找最近的拥有 'pdf-card' 类的父元素或自身
        const clickedCard = event.target.closest('.pdf-card');

        // 如果确实点击了某个 PDF 卡片
        if (clickedCard) {
            // 获取存储在卡片 data-pdf-url 属性中的 PDF URL
            currentPdfUrl = clickedCard.dataset.pdfUrl;

            if (currentPdfUrl) {
                // **在显示新选项前，先隐藏之前可能显示的查看器**
                pdfViewerContainer.style.display = 'none';
                // 清空 iframe 的 src 属性，停止加载任何之前显示的 PDF
                pdfIframe.src = '';

                // **显示阅读选项容器**
                pdfOptionsContainer.style.display = 'block';

                // 更新 iframe 备用链接的 href 属性，指向当前 PDF 的 URL
                pdfDownloadLink.href = currentPdfUrl;

                // 可选：滚动到选项容器的位置，让用户看到选项
                // pdfOptionsContainer.scrollIntoView({ behavior: 'smooth' });
            }
        }
    });

    // **为“原生阅读”按钮添加点击事件监听器**
    nativeReadBtn.addEventListener('click', function() {
        if (currentPdfUrl) {
            // 处理URL
            // https://roadmap.sh/backend
            const match = currentPdfUrl.match(/\/([^/?#]+)\.[^/.]+$/);
            const nameWithoutExt = match ? match[1] : null;

            console.log(nameWithoutExt); // 输出：backend
            // 使用 window.open() 在新标签页打开 PDF URL，依赖浏览器原生能力
            const finalUrl = `https://roadmap.sh/${nameWithoutExt}`;
            window.open(finalUrl, '_blank');

            // **在新标签页打开后，隐藏选项容器**
            pdfOptionsContainer.style.display = 'none';
            // 可选：清空当前选中的 URL
            // currentPdfUrl = null;
        }
    });

    // **为“离线阅读”按钮添加点击事件监听器 (当前实现为内嵌 iframe)**
    offlineReadBtn.addEventListener('click', function() {
        if (currentPdfUrl) {
            // **将当前 PDF 的 URL 设置为 iframe 的 src 属性，加载 PDF**
            pdfIframe.src = currentPdfUrl;

            // **显示 PDF 查看器容器**
            pdfViewerContainer.style.display = 'block';

            // **隐藏选项容器**
            pdfOptionsContainer.style.display = 'none';

            // 可选：滚动到查看器容器的位置
            // pdfViewerContainer.scrollIntoView({ behavior: 'smooth' });
            // 可选：清空当前选中的 URL
            // currentPdfUrl = null;
        }
    });
});