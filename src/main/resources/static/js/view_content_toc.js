document.addEventListener("DOMContentLoaded", function() {
    const contentElement = document.getElementById('post-article-content');
    const tocRootUlElement = document.getElementById('toc-list-ul');
    const tocNavContainer = document.getElementById('table-of-contents-container');

    const mainContentColumn = document.querySelector('.col-lg-9.col-md-8'); // 主内容列
    const tocColumnElement = document.querySelector('.col-lg-3.col-md-4');   // 目录列

    if (!contentElement || !tocRootUlElement || !tocNavContainer || !mainContentColumn || !tocColumnElement) {
        console.warn('TOC critical elements not found. Skipping TOC generation and hiding TOC column.');
        if(tocNavContainer) tocNavContainer.style.display = 'none';
        if(tocColumnElement) tocColumnElement.style.display = 'none';
        if(mainContentColumn) { // 如果目录列不存在或隐藏，主内容列扩展
            mainContentColumn.classList.remove('col-lg-9', 'col-md-8');
            mainContentColumn.classList.add('col-lg-12', 'col-md-12'); // 扩展到全宽
        }
        return;
    }

    const headingSelectors = '#post-article-content h1, #post-article-content h2, #post-article-content h3, #post-article-content h4, #post-article-content h5, #post-article-content h6';
    const headings = Array.from(document.querySelectorAll(headingSelectors));

    if (headings.length === 0) {
        if (tocNavContainer) tocNavContainer.style.display = 'none';
        if (tocColumnElement) tocColumnElement.style.display = 'none';
        if (mainContentColumn) {
            mainContentColumn.classList.remove('col-lg-9', 'col-md-8');
            mainContentColumn.classList.add('col-lg-12', 'col-md-12');
        }
        return;
    }

    const listStack = [tocRootUlElement]; // 栈来管理当前嵌套的ul

    headings.forEach(function(heading) {
        const id = heading.id;
        const text = heading.textContent.trim();
        const level = parseInt(heading.tagName.substring(1)); // h1 -> 1, h2 -> 2

        if (!id || !text) {
            console.warn('Skipping heading without ID or text:', heading.outerHTML);
            return;
        }

        // 根据级别调整列表嵌套
        // listStack 的长度代表当前所在的嵌套深度 (根ul算第1层)
        while (level > listStack.length) { // 需要更深的嵌套
            const parentLi = listStack[listStack.length - 1].lastElementChild;
            if (!parentLi) { // 如果父ul还没有li (理论上不应发生，除非是空的h(x-1))
                // 创建一个临时的父级li，这种情况比较罕见，通常每个ul都始于一个li
                const tempParentLi = document.createElement('li');
                listStack[listStack.length - 1].appendChild(tempParentLi);
                const newUl = document.createElement('ul');
                tempParentLi.appendChild(newUl);
                listStack.push(newUl);
            } else {
                const newUl = document.createElement('ul');
                parentLi.appendChild(newUl);
                listStack.push(newUl);
            }
        }
        while (level < listStack.length) { // 需要返回到上层嵌套
            listStack.pop();
        }

        const currentList = listStack[listStack.length - 1];
        const listItem = document.createElement('li');
        const link = document.createElement('a');
        link.href = '#' + id;
        link.textContent = text;

        listItem.appendChild(link);
        currentList.appendChild(listItem);
    });

    // --- Scrollspy JS ---
    const contentHeadingsForScrollspy = headings; // 我们已经获取了headings
    const tocLinksForScrollspy = document.querySelectorAll('.toc-sidebar ul a');
    const scrollSpyOffset = 80; // 偏移量，根据您的固定导航栏高度调整

    let currentlyActiveLink = null; // 用于存储当前高亮的链接元素

    function highlightTocLink() {
        let newActiveHeadingId = null;
        let bestMatch = { id: null, top: Infinity }; // 用来找最接近offset的标题

        for (let i = 0; i < contentHeadingsForScrollspy.length; i++) {
            const heading = contentHeadingsForScrollspy[i];
            const rect = heading.getBoundingClientRect();

            // 条件1: 标题的上边缘在偏移量下方，或者标题的下边缘在偏移量上方但标题仍在视口内
            //       这意味着标题的“主体”部分正在或即将通过偏移线附近
            if (rect.top <= scrollSpyOffset && rect.bottom > scrollSpyOffset / 3) { // 放宽一点底部判断
                // 如果多个标题满足，我们通常希望高亮最接近偏移线的那个
                // （即 rect.top 最接近 scrollSpyOffset 且不大于它的那个）
                // 或者简单地，从上往下第一个顶部低于offset的
                if (rect.top < bestMatch.top && rect.top <= scrollSpyOffset) { // 更新：找rect.top最接近offset且不大于它的
                    // 如果我们想要的是 "当前屏幕主要显示的段落对应的标题"
                    // 而不是 "刚刚滚过去的标题"
                    // 那么当标题的 rect.top > 0 并且 rect.top < offset 时，它是很好的候选
                    // 简单起见，我们还是用第一个顶部低于offset的标题
                    newActiveHeadingId = heading.id;
                    break; // 从上往下，第一个顶部低于offset的就激活它
                }
            }
        }

        // 如果没有找到新的激活标题（可能在两个标题之间的大段空白，或已滚过所有标题）
        // 并且页面不是在最顶部，我们尝试保持之前的高亮
        if (!newActiveHeadingId && window.scrollY > 0) {
            // 如果之前有高亮的链接，则保持它，除非有新的有效高亮
            if (currentlyActiveLink) {
                // 这里我们什么都不做，让 currentlyActiveLink 保持 active 状态
                // 但是，如果滚动到了最底部，最后一个标题也已经滚过去了，
                // 这种情况下可能希望最后一个目录项保持高亮。
                const lastHeading = contentHeadingsForScrollspy[contentHeadingsForScrollspy.length - 1];
                if (lastHeading && lastHeading.getBoundingClientRect().bottom < scrollSpyOffset) {
                    newActiveHeadingId = lastHeading.id; // 强制高亮最后一个
                } else {
                    return; // 没有新的激活项，也不满足特殊条件，则不改变当前高亮
                }
            } else if (contentHeadingsForScrollspy.length > 0) {
                // 如果之前没有高亮，且不在顶部，可能处于初始加载后的滚动，尝试高亮第一个
                // newActiveHeadingId = contentHeadingsForScrollspy[0].id;
            }
        } else if (!newActiveHeadingId && window.scrollY === 0 && tocLinksForScrollspy.length > 0) {
            // 滚动到页面最顶部，高亮第一个目录项
            newActiveHeadingId = contentHeadingsForScrollspy[0] ? contentHeadingsForScrollspy[0].id : null;
        }


        // 如果新的激活ID与当前的不同，或者当前没有激活的链接，则更新高亮
        const newActiveLink = newActiveHeadingId ? document.querySelector(`.toc-sidebar a[href="#${CSS.escape(newActiveHeadingId)}"]`) : null;

        if (newActiveLink !== currentlyActiveLink) {
            if (currentlyActiveLink) {
                currentlyActiveLink.classList.remove('active');
            }
            if (newActiveLink) {
                newActiveLink.classList.add('active');
                // 可选: 将激活的目录项滚动到目录侧边栏的可见区域
                // newActiveLink.scrollIntoView({ behavior: 'auto', block: 'nearest', inline: 'nearest' });
            }
            currentlyActiveLink = newActiveLink;
        }
    }

    // (window.addEventListener 和初始调用 highlightTocLink() 保持不变)
    if (contentHeadingsForScrollspy.length > 0 && tocLinksForScrollspy.length > 0) {
        window.addEventListener('scroll', highlightTocLink, { passive: true });
        highlightTocLink(); // 初始高亮
    }
});