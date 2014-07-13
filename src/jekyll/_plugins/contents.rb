module Rultor
  class ContentsBlock < Liquid::Tag
    def render(context)
      year = ''
      month = ''
      html = ''
      context['site']['posts'].each do |post|
        if post.date.year != year
          year = post.date.year
        end
        if post.date.month != month
          month = post.date.month
          html += "<h2>#{Date::MONTHNAMES[month]} #{year}</h2>"
        end
        html += "<p><a href='#{post.url}'>#{post.title}</a></p>"
      end
      html
    end
  end
end

Liquid::Template.register_tag("contents", Rultor::ContentsBlock)
